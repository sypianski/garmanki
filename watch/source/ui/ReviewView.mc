import Toybox.Lang;
import Toybox.Graphics;
import Toybox.WatchUi;
import Toybox.System;
import Toybox.Time;
import Toybox.Application.Storage;
import Toybox.Math;
import Toybox.Attention;

// One review session over a snapshot of a deck's cards.
//
// Card record (SCHEMA.md §2): [cid, nid, ord, deckIdx, front, back, [nt1..nt4]]
// Keys (D13/D14, mapping configurable via ActionMap): any grade key reveals;
// then defaults UP=Easy, DOWN=Good, START=Again; BACK or hold UP = Options
// menu: card actions plus only the grades no button/gesture reaches (GAR-06). Touch (D15/D16): tap reveals, then tap=Good; swipes
// →Good ←Again ↑Easy ↓Hard, also via ActionMap.
//
// Same-session learning queue (DECYZJE.md D3). The scheduler still lives in
// AnkiDroid — but within a sitting we mimic Anki's learning queue so a failed
// card comes back *interleaved*, not dumped at the tail. Two queues: `_new`
// (unseen cards, cursor `_ni`) and `_learn` (entries [dueMs, card, stepIdx]
// carrying a due timestamp from System.getTimer()). _pick() prefers a learn
// card whose due has passed, else the next new card, else — when no new cards
// remain — the soonest-due learn card early (Anki's "learn ahead"). The step
// length is borrowed straight from AnkiDroid: nt1 is the Again next-interval
// label (NEXT_REVIEW_TIMES), so we reuse the scheduler's own answer instead of
// recomputing it. Termination is guaranteed by STEP_CAP (a card re-shows at
// most STEP_CAP times). This changes only *presentation order* — the answer
// queue `q` still gets one row per keypress with the real (ease, timeMs, epoch).
//
// Design ("ink & paper"): thin progress ring around the screen, question in
// paper-white; on reveal the question gives way to the answer alone — the
// answer takes the whole middle band, hierarchy through light, not chrome. The only standing hint
// system is the color ticks on the bezel at the physical buttons, colored
// per mapped ease (Theme.easeColor); the onboarding guide (GAR-03) teaches
// the rest.
class ReviewView extends WatchUi.View {

    // A card re-shows at most this many times per session (guarantees the
    // sitting terminates even if every answer is "Again").
    private const STEP_CAP = 3;
    // Intervals at/above this (~90 min: hours, days, months) can't come due in
    // a sitting — treated as end-of-session via learn-ahead.
    private const SESSION_LONG = 5400000;
    private const DEFAULT_STEP = 60000; // fallback when nt1 is empty/unknown
    // Undo depth per sitting (TASK-41).
    private const UNDO_MAX = 10;
    // Options menu ids beside grades (1-4) and card actions (11-14).
    private const MENU_UNDO = 20;
    private const MENU_END = 21;

    private var _new as Array;          // unseen cards, cursor _ni
    private var _ni as Number = 0;
    private var _learn as Array = [];   // entries [dueMs, card, stepIdx]
    private var _cur as Array or Null;  // card currently on screen
    private var _curStep as Number = 0; // times _cur has already been failed
    private var _deckName as String;
    private var _showAnswer as Boolean = false;
    private var _done as Number = 0;
    private var _counts as Array = [0, 0, 0, 0]; // grades given this sitting: Again, Hard, Good, Easy
    private var _startMs as Number;
    private var _cardStartMs as Number;
    // Undo stack: [cur, curStep, ni, learn, done, counts, queuedRow or null].
    private var _history as Array = [];

    function initialize(cards as Array, deckName as String) {
        View.initialize();
        _new = cards;
        _deckName = deckName;
        _startMs = System.getTimer();
        _cardStartMs = _startMs;
        _pick(); // caller (DeckMenu) guarantees cards is non-empty
    }

    // Pick the next card into _cur/_curStep. Returns false when the session is
    // exhausted (no new cards, no pending learn cards).
    private function _pick() as Boolean {
        var t = System.getTimer();
        var bi = _soonestLearn();
        if (bi >= 0 && (_learn[bi] as Array)[0] <= t) {
            var e = _removeLearn(bi);
            _cur = e[1];
            _curStep = e[2];
            return true;
        }
        if (_ni < _new.size()) {
            _cur = _new[_ni];
            _ni++;
            _curStep = 0;
            return true;
        }
        if (bi >= 0) { // learn-ahead: no new cards left, show the soonest early
            var e = _removeLearn(bi);
            _cur = e[1];
            _curStep = e[2];
            return true;
        }
        _cur = null;
        return false;
    }

    // Index of the learn entry with the earliest due, or -1 when empty.
    private function _soonestLearn() as Number {
        if (_learn.size() == 0) {
            return -1;
        }
        var best = 0;
        for (var k = 1; k < _learn.size(); k++) {
            if ((_learn[k] as Array)[0] < (_learn[best] as Array)[0]) {
                best = k;
            }
        }
        return best;
    }

    private function _removeLearn(idx as Number) as Array {
        var e = _learn[idx] as Array;
        var out = [];
        for (var k = 0; k < _learn.size(); k++) {
            if (k != idx) {
                out.add(_learn[k]);
            }
        }
        _learn = out;
        return e;
    }

    // AnkiDroid next-interval label (e.g. "<1 min", "10 min", "3 d") → ms.
    // Days/months and anything ≥ SESSION_LONG are clamped so they only surface
    // via learn-ahead at the end; the "<" prefix shortens below one unit.
    private function _parseInterval(lbl) as Number {
        if (!(lbl instanceof String) || lbl.length() == 0) {
            return DEFAULT_STEP;
        }
        var mult;
        if (lbl.find("mo") != null || lbl.find("yr") != null ||
            lbl.find("d") != null || lbl.find("h") != null) {
            return SESSION_LONG; // hours+ won't come due in a sitting
        } else if (lbl.find("min") != null || lbl.find("m") != null) {
            mult = 60000;
        } else if (lbl.find("s") != null) {
            mult = 1000;
        } else {
            mult = 60000;
        }
        var n = 0;
        var seen = false;
        var chars = lbl.toCharArray();
        for (var i = 0; i < chars.size(); i++) {
            var cn = (chars[i] as Char).toNumber();
            if (cn >= 48 && cn <= 57) {
                n = n * 10 + (cn - 48);
                seen = true;
            } else if (seen) {
                break;
            }
        }
        if (!seen) {
            n = 1; // e.g. a bare "<min"; "<1 min" already carries the 1
        }
        var ms = n * mult;
        if (lbl.find("<") != null && ms > 30000) {
            ms = 30000; // "<1 min" → ~30 s, back sooner than a full minute
        }
        if (ms < 5000) {
            ms = 5000; // floor: never reappear effectively instantly
        }
        if (ms > SESSION_LONG) {
            ms = SESSION_LONG;
        }
        return ms;
    }

    // Ekran zegarka mieści realnie dwa pola. Pierwsza niepusta linia to pole
    // główne, druga (jeśli jest) idzie pod spodem mniejsza i szara — resztę
    // odrzucamy. HtmlToText.clean po stronie Androida gwarantuje, że pola
    // Anki (div/br/p) trafiają tu rozdzielone \n.
    private function _splitFields(text as String) as Array {
        if (!(text instanceof String) || text.length() == 0) {
            return ["", ""];
        }
        var primary = "";
        var extra = "";
        var chars = text.toCharArray();
        var buf = "";
        for (var i = 0; i < chars.size(); i++) {
            var c = chars[i];
            if (c.toString().equals("\n")) {
                if (buf.length() > 0) {
                    if (primary.length() == 0) {
                        primary = buf;
                    } else {
                        extra = buf;
                        break;
                    }
                    buf = "";
                } else {
                    buf = "";
                }
            } else {
                buf += c.toString();
            }
        }
        if (buf.length() > 0 && extra.length() == 0) {
            if (primary.length() == 0) {
                primary = buf;
            } else {
                extra = buf;
            }
        }
        return [primary, extra];
    }

    function onUpdate(dc as Dc) as Void {
        dc.setColor(Theme.BG, Theme.BG);
        dc.clear();

        var w = dc.getWidth();
        var h = dc.getHeight();
        var cx = w / 2;
        if (_cur == null) {
            return;
        }
        var card = _cur;

        // Progress over a queue that grows on each miss: done vs. what's left
        // (current card + unseen new + pending learn). The denominator climbs
        // when you fail a card — honest, since there is now more to do.
        var remaining = (_new.size() - _ni) + _learn.size();
        var known = _done + 1 + remaining;

        // Session progress: the same thin ring as on the home screen, swept
        // clockwise from 12 o'clock. It can step back after an Again, since the
        // denominator grows with the queue.
        Theme.ring(dc, cx - 12, known > 0 ? _done.toFloat() / known : 0.0);

        var counter = (_done + 1).toString() + "/" + known.toString();
        dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 6 / 100, Graphics.FONT_XTINY, counter,
            Graphics.TEXT_JUSTIFY_CENTER);
        // BACK opens the options menu (TASK-41).
        Theme.tick(dc, Theme.ANG_BACK, Theme.MUTED);
        if (_curStep > 0 && !_showAnswer) {
            // This card was failed earlier in the sitting and came back.
            _drawRepeatMark(dc,
                cx + dc.getTextWidthInPixels(counter, Graphics.FONT_XTINY) / 2
                    + Theme.px(dc, 16),
                h * 6 / 100 + dc.getFontHeight(Graphics.FONT_XTINY) / 2);
        }

        if (!_showAnswer) {
            var qp = _splitFields(card[4] as String);
            var qExtra = qp[1] as String;
            var qHasExtra = qExtra.length() > 0;
            var qMainH = qHasExtra ? h * 50 / 100 : h * 68 / 100;
            var q = new WatchUi.TextArea({
                :text => qp[0] as String,
                :color => Theme.PAPER,
                :font => [Graphics.FONT_LARGE, Graphics.FONT_MEDIUM,
                          Graphics.FONT_SMALL, Graphics.FONT_TINY, Graphics.FONT_XTINY],
                :locX => w / 10,
                :locY => h * 16 / 100,
                :width => w * 8 / 10,
                :height => qMainH,
                :justification => Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
            });
            q.draw(dc);
            if (qHasExtra) {
                var qx = new WatchUi.TextArea({
                    :text => qExtra,
                    :color => Theme.MUTED,
                    :font => [Graphics.FONT_TINY, Graphics.FONT_XTINY],
                    :locX => w / 8,
                    :locY => h * 68 / 100,
                    :width => w * 3 / 4,
                    :height => h * 16 / 100,
                    :justification => Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
                });
                qx.draw(dc);
            }
            Theme.tick(dc, Theme.ANG_START, Theme.ACCENT);
        } else {
            // Answer only: the question was just on screen, so the answer gets
            // the whole middle band (TASK-28.4).
            var ap = _splitFields(card[5] as String);
            var aExtra = ap[1] as String;
            var aHasExtra = aExtra.length() > 0;
            var aTop = h * 20 / 100;
            var aMainH = aHasExtra ? h * 40 / 100 : h * 58 / 100;
            var xTop = h * 64 / 100;
            var xH = h * 15 / 100;
            var tickMode = _tickMode();
            var labelFont = tickMode == 2 ? Graphics.FONT_TINY : Graphics.FONT_XTINY;
            if (tickMode != 0) {
                // Tick labels sit at the bezel rows of the buttons (30/150 deg
                // on top, 210 deg below), so the answer lives in the band
                // between them and the extra field below the lower row.
                var lfh = dc.getFontHeight(labelFont);
                var lr = cx - Theme.px(dc, 45);
                var pad = Theme.px(dc, 3);
                aTop = h / 2 - lr / 2 + lfh / 2 + pad;
                var aBottom = h * 80 / 100;
                if (DeviceProfile.HAS_UPDOWN_BUTTONS) {
                    aBottom = h / 2 + lr / 2 - lfh / 2 - pad;
                    xTop = h / 2 + lr / 2 + lfh / 2 + pad;
                    xH = Theme.px(dc, 46);
                } else if (aHasExtra) {
                    aBottom = h * 62 / 100;
                }
                aMainH = aBottom - aTop;
            }
            var a = new WatchUi.TextArea({
                :text => ap[0] as String,
                :color => Theme.PAPER,
                :font => [Graphics.FONT_LARGE, Graphics.FONT_MEDIUM,
                          Graphics.FONT_SMALL, Graphics.FONT_TINY, Graphics.FONT_XTINY],
                :locX => w / 10,
                :locY => aTop,
                :width => w * 8 / 10,
                :height => aMainH,
                :justification => Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
            });
            a.draw(dc);
            if (aHasExtra) {
                var ax = new WatchUi.TextArea({
                    :text => aExtra,
                    :color => Theme.MUTED,
                    :font => [Graphics.FONT_TINY, Graphics.FONT_XTINY],
                    :locX => w / 8,
                    :locY => xTop,
                    :width => w * 3 / 4,
                    :height => xH,
                    :justification => Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
                });
                ax.draw(dc);
            }
            // Ticks reflect the live mapping — button order: UP, DOWN, START.
            // Touch-first devices have no UP/DOWN pair; only the START tick.
            var events = DeviceProfile.HAS_UPDOWN_BUTTONS
                ? ["up", "down", "start"] : ["start"];
            var angles = DeviceProfile.HAS_UPDOWN_BUTTONS
                ? [Theme.ANG_UP, Theme.ANG_DOWN, Theme.ANG_START]
                : [Theme.ANG_START];
            for (var k = 0; k < events.size(); k++) {
                var ease = ActionMap.easeFor(events[k]);
                if (ease != null) {
                    Theme.tick(dc, angles[k], Theme.easeColor(ease));
                    var label = tickMode == 1 ? _intervalFor(card, ease)
                        : (tickMode == 2 ? Theme.easeWord(ease) : "");
                    if (label.length() > 0) {
                        Theme.tickLabel(dc, angles[k], label,
                            Theme.easeColor(ease), labelFont);
                    }
                }
            }
        }
    }

    // Small ACCENT "again" arrow (270 degrees of arc + arrowhead) centred at
    // (x, y). Drawn, not a glyph: built-in fonts don't guarantee ↻.
    private function _drawRepeatMark(dc as Dc, x as Number, y as Number) as Void {
        var r = Theme.px(dc, 7);
        dc.setColor(Theme.ACCENT, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(2);
        dc.drawArc(x, y, r, Graphics.ARC_COUNTER_CLOCKWISE, 70, 340);
        dc.setPenWidth(1);
        // Arrowhead at the arc's end (340 degrees), pointing along the travel.
        var ex = x + r * Math.cos(340 * Math.PI / 180.0);
        var ey = y - r * Math.sin(340 * Math.PI / 180.0);
        var dx = 0.342;
        var dy = -0.940;
        var len = Theme.px(dc, 6).toFloat();
        var half = Theme.px(dc, 4).toFloat();
        dc.fillPolygon([
            [(ex + dx * len).toNumber(), (ey + dy * len).toNumber()],
            [(ex - dy * half).toNumber(), (ey + dx * half).toNumber()],
            [(ex + dy * half).toNumber(), (ey - dx * half).toNumber()]
        ]);
    }

    // Bezel-mark mode set from the companion (SCHEMA.md §8 `tm`): 0 color only,
    // 1 + next interval (default), 2 + grade word.
    private function _tickMode() as Number {
        var tm = Storage.getValue("tickMode");
        return (tm instanceof Number && tm >= 0 && tm <= 2) ? tm : 1;
    }

    // Next-interval label AnkiDroid computed for this ease ("<1 min", "4 d"),
    // or "" when the card carries none.
    private function _intervalFor(card as Array, ease as Number) as String {
        var nt = card[6] as Array;
        var v = nt.size() >= ease ? nt[ease - 1] : "";
        return v instanceof String ? v : "";
    }

    function isRevealed() as Boolean {
        return _showAnswer;
    }

    function reveal() as Void {
        _showAnswer = true;
        WatchUi.requestUpdate();
    }

    // Grades reachable via a button or gesture stay out of the menu (GAR-06);
    // card actions are filtered by the companion-pushed "cardActions" list
    // (SCHEMA.md §8) — absent list means all four.
    // Options (BACK or hold UP, TASK-41): Undo first, End session last.
    function openMenu() as Void {
        var card = _cur;
        if (card == null) {
            return;
        }
        var nt = card[6] as Array;
        var items = [];
        if (_history.size() > 0) {
            items.add([WatchUi.loadResource(Rez.Strings.Undo) as String, MENU_UNDO]);
        }
        var easeStrs = [Rez.Strings.EaseAgain, Rez.Strings.EaseHard,
                        Rez.Strings.EaseGood, Rez.Strings.EaseEasy];
        for (var ease = 1; ease <= 4; ease++) {
            if (_showAnswer && !ActionMap.isMapped(ease)) {
                items.add([_gradeLabel(easeStrs[ease - 1], nt, ease - 1), ease]);
            }
        }
        var enabled = Storage.getValue("cardActions");
        var codes = ["susp", "bury", "flag", "del"];
        var labels = [Rez.Strings.ActSuspend, Rez.Strings.ActBury,
                      Rez.Strings.ActFlag, Rez.Strings.ActDelete];
        for (var k = 0; k < codes.size(); k++) {
            if (enabled instanceof Array && enabled.indexOf(codes[k]) < 0) {
                continue;
            }
            items.add([WatchUi.loadResource(labels[k]) as String, 11 + k]);
        }
        items.add([WatchUi.loadResource(Rez.Strings.EndSession) as String, MENU_END]);
        Compat.showMenu(items, self);
    }

    private function _gradeLabel(strId, nt as Array, idx as Number) as String {
        var label = WatchUi.loadResource(strId) as String;
        var iv = nt.size() > idx ? nt[idx] : "";
        if (iv instanceof String && iv.length() > 0) {
            label = label + " · " + iv;
        }
        return label;
    }

    function handleMenu(id) as Void {
        if (id instanceof Number) {
            if (id == MENU_UNDO) {
                undo();
            } else if (id == MENU_END) {
                endSession(false);
            } else if (id >= 1 && id <= 4) {
                grade(id);
            } else if (id == 11) {
                _action("susp");
            } else if (id == 12) {
                _action("bury");
            } else if (id == 13) {
                _action("flag");
            } else if (id == 14) {
                _action("del");
            }
        }
    }

    function grade(ease as Number) as Void {
        var card = _cur;
        var t = System.getTimer();
        var timeMs = t - _cardStartMs;
        if (timeMs > 60000) {
            timeMs = 60000;
        }
        var row = ["a", card[0], card[1], card[2], ease, timeMs, Time.now().value()];
        // Sample-deck answers stay on the watch (TASK-40).
        _snapshot(DemoDeck.isDemo(card as Array) ? null : row);
        if (!DemoDeck.isDemo(card as Array)) {
            CardStore.queueRow(row);
        }
        _done++;
        _counts[ease - 1] = (_counts[ease - 1] as Number) + 1;
        // "Again" → back into the learning queue with a due timestamp derived
        // from Anki's own Again interval (nt1). The step grows with each miss
        // and STEP_CAP bounds re-shows so the session always terminates. Any
        // grade ≥ Hard "graduates" the card — it simply isn't re-queued.
        if (ease == 1 && _curStep < STEP_CAP) {
            var nt = card[6] as Array;
            var base = _parseInterval(nt.size() > 0 ? nt[0] : "");
            var due = t + base * (_curStep + 1);
            _learn.add([due, card, _curStep + 1]);
        }
        _advance();
    }

    private function _action(code as String) as Void {
        var card = _cur;
        var row = ["x", card[0], card[1], card[2], code];
        // On the sample deck an action only affects this sitting (TASK-40).
        if (DemoDeck.isDemo(card as Array)) {
            _snapshot(null);
        } else {
            _snapshot(row);
            CardStore.queueRow(row);
        }
        if ("flag".equals(code)) {
            Compat.toast(Rez.Strings.Flagged);
            return; // flag keeps the card on screen (D4)
        }
        _advance();
    }

    // State before an answer/action, for undo. `row` = what went into q.
    private function _snapshot(row as Array?) as Void {
        _history.add([_cur, _curStep, _ni, _learn.slice(0, null), _done,
            _counts.slice(0, null), row]);
        if (_history.size() > UNDO_MAX) {
            _history = _history.slice(1, null);
        }
    }

    // Back to the previous card. A row the phone already has (or that is in
    // flight) can't be taken back — AnkiDroid's API has no undo.
    function undo() as Void {
        var n = _history.size();
        if (n == 0) {
            return;
        }
        var e = _history[n - 1] as Array;
        var row = e[6];
        if (row != null
                && !CardStore.dropLastIf(row as Array, Link.get().inFlightCount())) {
            _history = [];
            Compat.toast(Rez.Strings.UndoSynced);
            return;
        }
        _history = _history.slice(0, n - 1);
        _cur = e[0];
        _curStep = e[1] as Number;
        _ni = e[2] as Number;
        _learn = e[3] as Array;
        _done = e[4] as Number;
        _counts = e[5] as Array;
        _showAnswer = false;
        _cardStartMs = System.getTimer();
        WatchUi.requestUpdate();
    }

    private function _advance() as Void {
        _showAnswer = false;
        _cardStartMs = System.getTimer();
        if (!_pick()) {
            endSession(true);
            return;
        }
        WatchUi.requestUpdate();
    }

    // finished=true → session ran out of cards (switch to summary);
    // finished=false → user backed out mid-session (summary too, if any work done).
    function endSession(finished as Boolean) as Void {
        Link.get().flush();
        var minutes = (System.getTimer() - _startMs) / 60000;
        if (_done > 0) {
            if (finished) {
                _buzz();
            }
            WatchUi.switchToView(new SummaryView(_done, minutes, _counts),
                new SummaryDelegate(), WatchUi.SLIDE_LEFT);
        } else {
            WatchUi.popView(WatchUi.SLIDE_RIGHT);
        }
    }

    // One short pulse when the last card is graded, so the user can look
    // away during a session. Honors the watch's vibration setting; devices
    // without a motor lack Attention :vibrate.
    private function _buzz() as Void {
        if (Attention has :vibrate && System.getDeviceSettings().vibrateOn) {
            Attention.vibrate([new Attention.VibeProfile(60, 250)]);
        }
    }
}

class ReviewDelegate extends GestureDelegate {

    private var _review as ReviewView;

    function initialize(review as ReviewView) {
        GestureDelegate.initialize();
        _review = review;
    }

    // Any input reveals first; once revealed it grades per ActionMap.
    function onInput(event as String) as Void {
        if (!_review.isRevealed()) {
            _review.reveal();
            return;
        }
        var ease = ActionMap.easeFor(event);
        if (ease != null) {
            _review.grade(ease);
        }
    }

    // BACK and hold UP both open Options; End session lives there (TASK-41).
    function onHoldUp() as Void {
        _review.openMenu();
    }

    function onBack() as Boolean {
        _review.openMenu();
        return true;
    }
}
