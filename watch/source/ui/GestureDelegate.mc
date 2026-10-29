import Toybox.Lang;
import Toybox.System;
import Toybox.Timer;
import Toybox.WatchUi;

// One input event per user action, keyed like ActionMap: "start", "up",
// "down", "tap", "swipeL"/"swipeR"/"swipeU"/"swipeD" (TASK-39).
//
// Measured on fr965 with an input probe (log in TASK-39):
// - Buttons: onKeyPressed arrives 100-160 ms before the behavior
//   (ENTER → onSelect, UP → onPreviousPage, DOWN → onNextPage, ESC → onBack).
// - A tap never calls onTap; it arrives only as onSelect, with no key press.
// - onSwipe fired for 1 swipe in 9, but every swipe delivers onDrag
//   START/STOP — the direction is computed here from the drag vector.
// - Vertical drags sometimes also fire onPreviousPage / onNextPage.
// - Holding a button gives onKeyPressed only — no onKeyReleased, no
//   behavior, and never onMenu. Hold UP is detected here with a timer.
// So a behavior counts as a button only when its key was pressed just
// before; otherwise it came from touch and the drag already handled it.
class GestureDelegate extends WatchUi.BehaviorDelegate {

    // Key press → behavior took ≤ 160 ms on fr965.
    const KEY_WINDOW_MS = 600;
    // Drag and swipe (or onTap and onSelect) reporting the same touch.
    const DUP_MS = 400;
    // UP held this long without a release or behavior = hold UP.
    const HOLD_MS = 700;

    private var _key as Number? = null;
    private var _keyAt as Number = 0;
    private var _dragFrom as Array<Number>? = null;
    private var _swipeAt as Number = -100000;
    private var _tapAt as Number = -100000;
    private var _holdTimer as Timer.Timer?;
    private var _holdAt as Number = -100000;

    function initialize() {
        BehaviorDelegate.initialize();
    }

    // Override: one call per user action.
    function onInput(event as String) as Void {
    }

    // Override: UP held (the review screen opens its card menu).
    function onHoldUp() as Void {
    }

    function onKeyPressed(evt as WatchUi.KeyEvent) as Boolean {
        _key = evt.getKey();
        _keyAt = System.getTimer();
        if (_key == WatchUi.KEY_UP) {
            _cancelHold();
            _holdTimer = new Timer.Timer();
            _holdTimer.start(method(:onHoldTimer), HOLD_MS, false);
        }
        return false; // let the behavior follow
    }

    function onKeyReleased(evt as WatchUi.KeyEvent) as Boolean {
        if (evt.getKey() == WatchUi.KEY_UP) {
            _cancelHold();
        }
        return false;
    }

    function onHoldTimer() as Void {
        _holdTimer = null;
        _key = null; // a late behavior must not count as a short UP
        _holdUp();
    }

    // Watches that do send onMenu for hold UP; deduplicated with the timer.
    function onMenu() as Boolean {
        _cancelHold();
        _holdUp();
        return true;
    }

    private function _holdUp() as Void {
        var now = System.getTimer();
        if (now - _holdAt >= 1000) {
            _holdAt = now;
            onHoldUp();
        }
    }

    private function _cancelHold() as Void {
        if (_holdTimer != null) {
            _holdTimer.stop();
            _holdTimer = null;
        }
    }

    private function _fromKey(key as Number) as Boolean {
        if (_key == key && System.getTimer() - _keyAt < KEY_WINDOW_MS) {
            _key = null;
            return true;
        }
        return false;
    }

    function onSelect() as Boolean {
        if (_fromKey(WatchUi.KEY_ENTER)) {
            onInput("start");
        } else if (System.getTimer() - _tapAt >= DUP_MS) {
            onInput("tap");
        }
        return true;
    }

    function onNextPage() as Boolean {
        if (_fromKey(WatchUi.KEY_DOWN)) {
            onInput("down");
        }
        return true;
    }

    function onPreviousPage() as Boolean {
        _cancelHold();
        if (_fromKey(WatchUi.KEY_UP)) {
            onInput("up");
        }
        return true;
    }

    // Not delivered on fr965; kept for watches that do, deduplicated
    // against the onSelect that may follow.
    function onTap(evt as WatchUi.ClickEvent) as Boolean {
        _tapAt = System.getTimer();
        onInput("tap");
        return true;
    }

    function onDrag(evt as WatchUi.DragEvent) as Boolean {
        var type = evt.getType();
        if (type == WatchUi.DRAG_TYPE_START) {
            _dragFrom = evt.getCoordinates();
        } else if (type == WatchUi.DRAG_TYPE_STOP && _dragFrom != null) {
            var event = _direction(_dragFrom, evt.getCoordinates());
            _dragFrom = null;
            if (event != null) {
                _swipeAt = System.getTimer();
                onInput(event);
            }
        }
        return true;
    }

    // Fallback for a swipe without drag events; usually the drag got there
    // first. Always consumed, so an edge swipe never pops the view.
    function onSwipe(evt as WatchUi.SwipeEvent) as Boolean {
        if (System.getTimer() - _swipeAt < DUP_MS) {
            return true;
        }
        var dir = evt.getDirection();
        var event = "swipeD";
        if (dir == WatchUi.SWIPE_RIGHT) {
            event = "swipeR";
        } else if (dir == WatchUi.SWIPE_LEFT) {
            event = "swipeL";
        } else if (dir == WatchUi.SWIPE_UP) {
            event = "swipeU";
        }
        _swipeAt = System.getTimer();
        onInput(event);
        return true;
    }

    function onFlick(evt as WatchUi.FlickEvent) as Boolean {
        return true; // same touch as the drag
    }

    // Dominant axis of the drag; shorter than 15 % of the screen width is
    // not a swipe (a tap comes as onSelect anyway).
    private function _direction(from as Array<Number>, to as Array<Number>) as String? {
        var dx = to[0] - from[0];
        var dy = to[1] - from[1];
        var min = System.getDeviceSettings().screenWidth * 15 / 100;
        var ax = dx < 0 ? -dx : dx;
        var ay = dy < 0 ? -dy : dy;
        if (ax < min && ay < min) {
            return null;
        }
        if (ax >= ay) {
            return dx > 0 ? "swipeR" : "swipeL";
        }
        return dy > 0 ? "swipeD" : "swipeU";
    }
}
