import Toybox.Lang;
import Toybox.Application.Storage;

// Persistent state — SCHEMA.md §5.
//
// Card record (§2): [cid, nid, ord, deckIdx, front, back, [nt1..nt4]]
// Queue row: ["a", cid, nid, ord, ease, timeMs, epochSec]
//         or ["x", cid, nid, ord, action]
module CardStore {

    const MAX_CHUNK_CHARS = 24000; // stay well under the 32 KB Storage cap
    const MAX_CARDS = 400;

    function getRev() as Number? {
        var r = Storage.getValue("rev");
        return r instanceof Number ? r : null;
    }

    function getDecks() as Array {
        var d = Storage.getValue("decks");
        return d instanceof Array ? d : [];
    }

    function getStats() as Array {
        var s = Storage.getValue("stats");
        return (s instanceof Array && s.size() >= 2) ? s : [0, 0];
    }

    // Replace the whole state (no incremental merge in v1 — SCHEMA.md §4).
    // `cards_n` is written last so a torn write leaves a readable prefix.
    function replaceAll(decks as Array, cards as Array, stats, rev as Number) as Void {
        var oldN = Storage.getValue("cards_n");
        if (oldN instanceof Number) {
            for (var i = 0; i < oldN; i++) {
                Storage.deleteValue("cards_" + i.toString());
            }
        }
        if (cards.size() > MAX_CARDS) {
            cards = cards.slice(0, MAX_CARDS);
        }
        var nChunks = 0;
        var cur = [];
        var chars = 0;
        for (var i = 0; i < cards.size(); i++) {
            var c = cards[i];
            var est = 40 + (c[4] as String).length() + (c[5] as String).length();
            if (cur.size() > 0 && chars + est > MAX_CHUNK_CHARS) {
                Storage.setValue("cards_" + nChunks.toString(), cur);
                nChunks++;
                cur = [];
                chars = 0;
            }
            cur.add(c);
            chars += est;
        }
        if (cur.size() > 0) {
            Storage.setValue("cards_" + nChunks.toString(), cur);
            nChunks++;
        }
        Storage.setValue("decks", decks);
        if (stats instanceof Array) {
            Storage.setValue("stats", stats);
        }
        Storage.setValue("rev", rev);
        Storage.setValue("cards_n", nChunks);
    }

    // Cards of one deck, minus those already consumed by a queued answer or
    // a removing action. A queued "flag" keeps the card available (D4).
    function cardsForDeck(deckIdx as Number) as Array {
        var gone = {};
        var q = rows();
        for (var i = 0; i < q.size(); i++) {
            var r = q[i];
            if ("a".equals(r[0]) || !"flag".equals(r[4])) {
                gone[r[1]] = true;
            }
        }
        var out = [];
        var n = Storage.getValue("cards_n");
        if (!(n instanceof Number)) {
            return out;
        }
        for (var i = 0; i < n; i++) {
            var chunk = Storage.getValue("cards_" + i.toString());
            if (!(chunk instanceof Array)) {
                continue;
            }
            for (var j = 0; j < chunk.size(); j++) {
                var c = chunk[j];
                if (c[3] == deckIdx && !gone.hasKey(c[0])) {
                    out.add(c);
                }
            }
        }
        return out;
    }

    // ---- answer/action queue ----

    function rows() as Array {
        var q = Storage.getValue("q");
        return q instanceof Array ? q : [];
    }

    function pendingCount() as Number {
        return rows().size();
    }

    function queueRow(row as Array) as Void {
        var q = rows();
        q.add(row);
        Storage.setValue("q", q);
    }

    function getLastSyncTime() as Number? {
        // Storage roundtrip may promote a Number epoch to Long on some CIQ
        // runtimes (fr9xx has been observed doing this). Long is NOT a
        // subtype of Number in Monkey C — both inherit Numeric — so a naive
        // `instanceof Number` check silently dropped valid stamps, hiding
        // the sync-time line on HomeView after a successful sync. Accept
        // any Numeric and normalize down to Number.
        var t = Storage.getValue("lastSync");
        if (t instanceof Number) { return t; }
        if (t instanceof Long) { return (t as Long).toNumber(); }
        return null;
    }

    function setLastSyncTime(epochSec as Number) as Void {
        Storage.setValue("lastSync", epochSec);
    }

    // Undo of the last answer/action (TASK-41): drop the tail row only if it
    // is still that row and not part of the batch in flight. Compared via
    // toString — a Storage roundtrip may turn Number into Long.
    function dropLastIf(row as Array, inFlight as Number) as Boolean {
        var q = rows();
        var n = q.size();
        if (n <= inFlight) {
            return false;
        }
        var last = q[n - 1] as Array;
        if (last.size() != row.size()) {
            return false;
        }
        for (var i = 0; i < row.size(); i++) {
            if (!(last[i] as Object).toString().equals((row[i] as Object).toString())) {
                return false;
            }
        }
        Storage.setValue("q", q.slice(0, n - 1) as Array<Storage.ValueType>);
        return true;
    }

    function dropFirst(n as Number) as Void {
        var q = rows();
        Storage.setValue("q", n >= q.size() ? [] : q.slice(n, null));
    }

    // Batch id of the snapshot currently (or next) in flight. Bumped only
    // after the companion acks — retries reuse the id (SCHEMA.md §3).
    function currentBatch() as Number {
        var b = Storage.getValue("batch");
        return b instanceof Number ? b : 1;
    }

    function bumpBatch() as Void {
        Storage.setValue("batch", currentBatch() + 1);
    }

    // Rows frozen into the current batch, persisted so a resend after an app
    // restart carries exactly the same rows under the same id (TASK-43): the
    // companion re-acks a known batch without applying it, so extra rows in
    // it would be dropped unseen. 0 = nothing sent yet.
    function sentCount() as Number {
        var s = Storage.getValue("sent");
        if (s instanceof Array && s.size() == 2 && currentBatch().equals(s[0])) {
            var n = s[1];
            return n instanceof Number ? n : 0;
        }
        return 0;
    }

    function setSent(count as Number) as Void {
        if (count > 0) {
            Storage.setValue("sent", [currentBatch(), count]);
        } else {
            Storage.deleteValue("sent");
        }
    }
}
