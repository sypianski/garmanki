import Toybox.Lang;
import Toybox.Graphics;
import Toybox.WatchUi;
import Toybox.System;
import Toybox.Time;
import Toybox.Application.Storage;

// One review session over a snapshot of a deck's cards.
//
// Card record (SCHEMA.md §2): [cid, nid, ord, deckIdx, front, back, [nt1..nt4]]
// Keys (D13/D14, mapping configurable via ActionMap): any grade key reveals;
// then defaults UP=Easy, DOWN=Good, START=Again; hold UP (onMenu) = action
// menu: card actions plus only the grades no button/gesture reaches (GAR-06). Touch (D15/D16): tap reveals, then tap=Good; swipes
// →Good ←Again ↑Easy ↓Hard, also via ActionMap. "Again" re-queues the card
// locally (max twice) so learning steps get a same-session second pass
// (DECYZJE.md D3).
//
// Design ("ink & paper"): 120° progress arc along the top bezel, question in
// paper-white; on reveal the question dims to warm gray and the answer takes
// the ink — hierarchy through light, not chrome. The only standing hint
// system is the color ticks on the bezel at the physical buttons, colored
// per mapped ease (Theme.easeColor); the onboarding guide (GAR-03) teaches
// the rest.
class ReviewView extends WatchUi.View {

    private var _cards as Array;
    private var _deckName as String;
    private var _i as Number = 0;
    private var _showAnswer as Boolean = false;
    private var _done as Number = 0;
    private var _startMs as Number;
    private var _cardStartMs as Number;
    private var _repeats = {};

    function initialize(cards as Array, deckName as String) {
        View.initialize();
        _cards = cards;
        _deckName = deckName;
        _startMs = System.getTimer();
        _cardStartMs = _startMs;
    }

    function onUpdate(dc as Dc) as Void {
        dc.setColor(Theme.BG, Theme.BG);
        dc.clear();

        var w = dc.getWidth();
        var h = dc.getHeight();
        var cx = w / 2;
        var card = _cards[_i];

        // Session progress: 120° arc across the top of the bezel (150°→30°).
        var r = cx - 5;
        dc.setPenWidth(3);
        dc.setColor(Theme.FAINT, Graphics.COLOR_TRANSPARENT);
        dc.drawArc(cx, h / 2, r, Graphics.ARC_CLOCKWISE, 150, 30);
        var frac = _cards.size() > 0 ? _i.toFloat() / _cards.size() : 0.0;
        if (frac > 0.0) {
            dc.setPenWidth(5);
            dc.setColor(Theme.ACCENT, Graphics.COLOR_TRANSPARENT);
            dc.drawArc(cx, h / 2, r, Graphics.ARC_CLOCKWISE, 150,
                150 - (120.0 * frac).toNumber());
        }
        dc.setPenWidth(1);

        dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 6 / 100, Graphics.FONT_XTINY,
            (_i + 1).toString() + "/" + _cards.size().toString(),
            Graphics.TEXT_JUSTIFY_CENTER);

        if (!_showAnswer) {
            var q = new WatchUi.TextArea({
                :text => card[4] as String,
                :color => Theme.PAPER,
                :font => [Graphics.FONT_LARGE, Graphics.FONT_MEDIUM,
                          Graphics.FONT_SMALL, Graphics.FONT_TINY, Graphics.FONT_XTINY],
                :locX => w / 10,
                :locY => h * 16 / 100,
                :width => w * 8 / 10,
                :height => h * 68 / 100,
                :justification => Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
            });
            q.draw(dc);
            Theme.tick(dc, Theme.ANG_START, Theme.ACCENT);
        } else {
            // Question recedes, answer takes the ink.
            var q = new WatchUi.TextArea({
                :text => card[4] as String,
                :color => Theme.MUTED,
                :font => [Graphics.FONT_TINY, Graphics.FONT_XTINY],
                :locX => w / 8,
                :locY => h * 14 / 100,
                :width => w * 3 / 4,
                :height => h * 22 / 100,
                :justification => Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
            });
            q.draw(dc);
            dc.setColor(Theme.ACCENT, Graphics.COLOR_TRANSPARENT);
            dc.fillRectangle(cx - 22, h * 39 / 100, 44, 3);
            var a = new WatchUi.TextArea({
                :text => card[5] as String,
                :color => Theme.PAPER,
                :font => [Graphics.FONT_LARGE, Graphics.FONT_MEDIUM,
                          Graphics.FONT_SMALL, Graphics.FONT_TINY, Graphics.FONT_XTINY],
                :locX => w / 10,
                :locY => h * 43 / 100,
                :width => w * 8 / 10,
                :height => h * 42 / 100,
                :justification => Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
            });
            a.draw(dc);
            // Ticks reflect the live mapping — button order: UP, DOWN, START.
            var events = ["up", "down", "start"];
            var angles = [Theme.ANG_UP, Theme.ANG_DOWN, Theme.ANG_START];
            for (var k = 0; k < events.size(); k++) {
                var ease = ActionMap.easeFor(events[k]);
                if (ease != null) {
                    Theme.tick(dc, angles[k], Theme.easeColor(ease));
                }
            }
        }
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
    function openMenu() as Void {
        var card = _cards[_i];
        var nt = card[6] as Array;
        var menu = new WatchUi.ActionMenu({});
        var items = 0;
        var easeStrs = [Rez.Strings.EaseAgain, Rez.Strings.EaseHard,
                        Rez.Strings.EaseGood, Rez.Strings.EaseEasy];
        for (var ease = 1; ease <= 4; ease++) {
            if (!ActionMap.isMapped(ease)) {
                menu.addItem(new WatchUi.ActionMenuItem(
                    {:label => _gradeLabel(easeStrs[ease - 1], nt, ease - 1)}, ease));
                items++;
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
            menu.addItem(new WatchUi.ActionMenuItem(
                {:label => WatchUi.loadResource(labels[k]) as String}, 11 + k));
            items++;
        }
        if (items == 0) {
            return; // everything mapped + all card actions disabled
        }
        WatchUi.showActionMenu(menu, new ReviewMenuDelegate(self));
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
            if (id >= 1 && id <= 4) {
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
        var card = _cards[_i];
        var timeMs = System.getTimer() - _cardStartMs;
        if (timeMs > 60000) {
            timeMs = 60000;
        }
        CardStore.queueRow(["a", card[0], card[1], card[2], ease, timeMs, Time.now().value()]);
        _done++;
        if (ease == 1) {
            var cid = card[0];
            var n = _repeats.hasKey(cid) ? _repeats[cid] : 0;
            if (n < 2) {
                _repeats[cid] = n + 1;
                _cards.add(card); // same-session second pass
            }
        }
        _advance();
    }

    private function _action(code as String) as Void {
        var card = _cards[_i];
        CardStore.queueRow(["x", card[0], card[1], card[2], code]);
        if ("flag".equals(code)) {
            WatchUi.showToast(Rez.Strings.Flagged, null);
            return; // flag keeps the card on screen (D4)
        }
        _advance();
    }

    private function _advance() as Void {
        _i++;
        _showAnswer = false;
        _cardStartMs = System.getTimer();
        if (_i >= _cards.size()) {
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
            WatchUi.switchToView(new SummaryView(_done, minutes),
                new SummaryDelegate(), WatchUi.SLIDE_LEFT);
        } else {
            WatchUi.popView(WatchUi.SLIDE_RIGHT);
        }
    }
}

class ReviewDelegate extends WatchUi.BehaviorDelegate {

    function initialize() {
        BehaviorDelegate.initialize();
    }

    private function _view() as ReviewView {
        return WatchUi.getCurrentView()[0] as ReviewView;
    }

    // Any grade key reveals first; once revealed it grades per ActionMap.
    private function _key(event as String) as Boolean {
        var v = _view();
        if (!v.isRevealed()) {
            v.reveal();
            return true;
        }
        var ease = ActionMap.easeFor(event);
        if (ease != null) {
            v.grade(ease);
        }
        return true;
    }

    function onSelect() as Boolean { // START
        return _key("start");
    }

    function onPreviousPage() as Boolean { // UP
        return _key("up");
    }

    function onNextPage() as Boolean { // DOWN
        return _key("down");
    }

    function onMenu() as Boolean { // hold UP
        var v = _view();
        if (v.isRevealed()) {
            v.openMenu();
        } else {
            v.reveal();
        }
        return true;
    }

    function onTap(clickEvent as WatchUi.ClickEvent) as Boolean {
        return _key("tap");
    }

    // Unmapped directions fall through (return false) so the system keeps
    // its own gesture; a mapped SWIPE_RIGHT returns true, which on fr965
    // should pre-empt the system back-swipe — needs on-device confirmation.
    function onSwipe(swipeEvent as WatchUi.SwipeEvent) as Boolean {
        var dir = swipeEvent.getDirection();
        var event = null;
        if (dir == WatchUi.SWIPE_RIGHT) {
            event = "swipeR";
        } else if (dir == WatchUi.SWIPE_LEFT) {
            event = "swipeL";
        } else if (dir == WatchUi.SWIPE_UP) {
            event = "swipeU";
        } else if (dir == WatchUi.SWIPE_DOWN) {
            event = "swipeD";
        }
        if (event == null) {
            return false;
        }
        var v = _view();
        if (!v.isRevealed()) {
            v.reveal();
            return true;
        }
        var ease = ActionMap.easeFor(event);
        if (ease == null) {
            return false;
        }
        v.grade(ease);
        return true;
    }

    function onBack() as Boolean {
        _view().endSession(false);
        return true;
    }
}

class ReviewMenuDelegate extends WatchUi.ActionMenuDelegate {

    private var _review as ReviewView;

    function initialize(review as ReviewView) {
        ActionMenuDelegate.initialize();
        _review = review;
    }

    function onSelect(item as WatchUi.ActionMenuItem) as Void {
        _review.handleMenu(item.getId());
    }

    function onBack() as Void {
    }
}
