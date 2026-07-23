import Toybox.Lang;
import Toybox.Communications;
import Toybox.System;
import Toybox.Time;
import Toybox.Timer;
import Toybox.WatchUi;
import Toybox.Application.Storage;

module Link {
    var _instance = null;

    function get() as PhoneLink {
        if (_instance == null) {
            _instance = new PhoneLink();
        }
        return _instance;
    }
}

// BLE link to the Android companion — SCHEMA.md §3/§4.
//
// Incoming: state pushes (t:"s", chunked, assembled like the
// notes_and_codes PhoneSync) and answer acks (t:"aa").
// Outgoing: hello, answer batches, state acks.
class PhoneLink {

    const CHUNK_TTL_MS = 60000;
    const AA_RETRY_MS = 30000;
    // Protocol/contract version — SCHEMA.md §1. Sent in hello, compared
    // against the companion's pv from state-push seq:1.
    const PROTOCOL_VERSION = 1;

    // state-push assembly
    private var _rev = null;
    private var _of = 0;
    private var _chunks = {};
    private var _decks = null;
    private var _stats = null;
    private var _cfg = null;
    private var _startedMs = 0;

    // answers batch in flight. The batch snapshot is immutable: a retry
    // resends the SAME first `_sentCount` rows under the SAME batch id, so
    // the companion's lastAppliedBatch dedup is always correct.
    private var _sentBatch = null;
    private var _sentCount = 0;
    private var _sentMs = 0;

    public var status as String = "";
    private var _statusTimer as Timer.Timer? = null;

    function initialize() {
    }

    // Show a transient status message; auto-clears after 5 s so the sync
    // time takes over on the home screen.
    function setStatus(s as String) as Void {
        status = s;
        WatchUi.requestUpdate();
        if (_statusTimer != null) {
            (_statusTimer as Timer.Timer).stop();
        }
        _statusTimer = new Timer.Timer();
        (_statusTimer as Timer.Timer).start(method(:_clearStatus), 5000, false);
    }

    function _clearStatus() as Void {
        status = "";
        _statusTimer = null;
        WatchUi.requestUpdate();
    }

    function onMessage(msg as Communications.PhoneAppMessage) as Void {
        var d = msg.data;
        if (!(d instanceof Dictionary) || d["p"] != 1) {
            return;
        }
        var t = d["t"];
        if (t != null && "s".equals(t)) {
            _onState(d);
        } else if (t != null && "aa".equals(t)) {
            _onAnswersAck(d);
        }
    }

    function hello() as Void {
        var pend = CardStore.pendingCount();
        _tx({"p" => 1, "t" => "h", "pv" => PROTOCOL_VERSION,
             "rev" => CardStore.getRev(), "pend" => pend});
        if (pend > 0) {
            flush();
        }
    }

    function flush() as Void {
        var now = System.getTimer();
        var q = CardStore.rows();
        var count;
        if (_sentBatch != null) {
            if (now - _sentMs < AA_RETRY_MS) {
                return; // in flight — wait for the ack
            }
            count = _sentCount; // retry: frozen snapshot
        } else {
            if (q.size() == 0) {
                return;
            }
            count = q.size();
        }
        if (count > q.size()) { // defensive; should not happen
            count = q.size();
        }
        var ans = [];
        var act = [];
        for (var i = 0; i < count; i++) {
            var r = q[i];
            if ("a".equals(r[0])) {
                ans.add(r.slice(1, null));
            } else {
                act.add(r.slice(1, null));
            }
        }
        _sentBatch = CardStore.currentBatch();
        _sentCount = count;
        _sentMs = now;
        _tx({"p" => 1, "t" => "a", "batch" => _sentBatch, "ans" => ans, "act" => act});
    }

    private function _onAnswersAck(d as Dictionary) as Void {
        var batch = d["batch"];
        if (_sentBatch == null || !(batch instanceof Number) || batch != _sentBatch) {
            return;
        }
        if (d["ok"] == true) {
            CardStore.dropFirst(_sentCount);
            CardStore.bumpBatch();
            setStatus(WatchUi.loadResource(Rez.Strings.SyncAnswersOk) as String);
        }
        _sentBatch = null;
        _sentCount = 0;
        flush(); // rows queued while the batch was in flight, if any
    }

    // ---- state push assembly ----

    private function _onState(d as Dictionary) as Void {
        var seq = d["seq"];
        var of = d["of"];
        var rev = d["rev"];
        if (!(seq instanceof Number) || !(of instanceof Number) || !(rev instanceof Number)
            || seq < 1 || of < 1 || seq > of) {
            return;
        }
        var now = System.getTimer();
        if (_rev != null && now - _startedMs > CHUNK_TTL_MS) {
            _resetAsm(); // stale partial set
        }
        if (seq == 1) {
            _resetAsm();
            _rev = rev;
            _of = of;
            _startedMs = now;
            _decks = d["decks"];
            _stats = d["stats"];
            _cfg = d["cfg"];
            // Soft protocol-version check (SCHEMA.md §1): warn but keep going.
            // An older companion may omit pv — treat that as compatible.
            var pv = d["pv"];
            if (pv instanceof Number && pv != PROTOCOL_VERSION) {
                setStatus(WatchUi.loadResource(Rez.Strings.SyncVersionMismatch) as String);
            }
        } else if (_rev == null || rev != _rev || of != _of) {
            return; // orphan chunk
        }
        var cards = d["cards"];
        _chunks[seq] = (cards instanceof Array) ? cards : [];
        for (var s = 1; s <= _of; s++) {
            if (!_chunks.hasKey(s)) {
                return;
            }
        }
        _assemble();
    }

    private function _assemble() as Void {
        var rev = _rev;
        var decks = _decks;
        var stats = _stats;
        var cfg = _cfg;
        var all = [];
        var ok = decks instanceof Array;
        for (var s = 1; s <= _of && ok; s++) {
            var part = _chunks[s];
            for (var j = 0; j < part.size(); j++) {
                var c = part[j];
                if (!(c instanceof Array) || c.size() < 7) {
                    ok = false;
                    break;
                }
                all.add(c);
            }
        }
        _resetAsm();
        if (!ok) {
            _ack(rev, false, "malformed");
            return;
        }
        CardStore.replaceAll(decks, all, stats, rev);
        CardStore.setLastSyncTime(Time.now().value());
        _applyCfg(cfg);
        setStatus(WatchUi.loadResource(Rez.Strings.SyncApplied) as String);
        _ack(rev, true, null);
    }

    // SCHEMA.md §8: sticky watch-UI config riding on chunk seq:1. Each part
    // is optional; an absent part leaves the stored value untouched.
    private function _applyCfg(cfg) as Void {
        if (!(cfg instanceof Dictionary)) {
            return;
        }
        var am = cfg["am"];
        if (am instanceof Dictionary) {
            Storage.setValue(ActionMap.STORAGE_KEY, am);
            ActionMap.reload();
        }
        var ca = cfg["ca"];
        if (ca instanceof Array) {
            Storage.setValue("cardActions", ca);
        }
        var gr = cfg["gr"];
        if (gr instanceof Number) {
            var seen = Storage.getValue("guideReset");
            if (seen instanceof Number && seen != gr) {
                // Companion asked for a guide replay (SCHEMA.md §8).
                Storage.deleteValue("onboardingSeen");
            }
            Storage.setValue("guideReset", gr);
        }
    }

    private function _resetAsm() as Void {
        _rev = null;
        _of = 0;
        _chunks = {};
        _decks = null;
        _stats = null;
        _cfg = null;
        _startedMs = 0;
    }

    private function _ack(rev, ok as Boolean, err) as Void {
        var payload = {"p" => 1, "ack" => rev, "ok" => ok};
        if (err != null) {
            payload["err"] = err;
        }
        _tx(payload);
    }

    private function _tx(payload as Dictionary) as Void {
        Communications.transmit(payload, null, new TxListener(self));
    }
}

class TxListener extends Communications.ConnectionListener {
    private var _link as PhoneLink;

    function initialize(link as PhoneLink) {
        ConnectionListener.initialize();
        _link = link;
    }

    function onComplete() as Void {
    }

    function onError() as Void {
        _link.setStatus(WatchUi.loadResource(Rez.Strings.SyncNoPhone) as String);
    }
}
