import Toybox.Graphics;
import Toybox.Lang;
import Toybox.Timer;
import Toybox.WatchUi;

// API-level shims (TASK-38). ActionMenu, showToast and getCurrentView arrive
// in Connect IQ 3.4.0; vivoactive4/4s stop at 3.3.x. monkeyc does not check
// a symbol's minimum API level, so the 3.4 path must not even be compiled
// for those devices: monkey.jungle excludes :legacy by default and :modern
// on API 3.3 devices.
module Compat {

    // items: [[label as String, id as Number], ...]
    (:modern)
    function showMenu(items as Array, review as ReviewView) as Void {
        var menu = new WatchUi.ActionMenu({});
        for (var i = 0; i < items.size(); i++) {
            menu.addItem(new WatchUi.ActionMenuItem(
                {:label => items[i][0] as String}, items[i][1] as Number));
        }
        WatchUi.showActionMenu(menu, new ReviewMenuDelegate(review));
    }

    (:legacy)
    function showMenu(items as Array, review as ReviewView) as Void {
        var menu = new WatchUi.Menu2({});
        for (var i = 0; i < items.size(); i++) {
            menu.addItem(new WatchUi.MenuItem(items[i][0] as String, null,
                items[i][1] as Number, {}));
        }
        WatchUi.pushView(menu, new ReviewMenuDelegate(review), WatchUi.SLIDE_UP);
    }

    (:modern)
    function toast(strId as ResourceId) as Void {
        WatchUi.showToast(strId, null);
    }

    (:legacy)
    function toast(strId as ResourceId) as Void {
        WatchUi.pushView(new ToastView(strId), new WatchUi.BehaviorDelegate(),
            WatchUi.SLIDE_IMMEDIATE);
    }
}

(:modern)
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

// Menu2 stays open after a selection (ActionMenu closes itself), so pop it
// before acting — handleMenu may switch to the summary or push a toast.
(:legacy)
class ReviewMenuDelegate extends WatchUi.Menu2InputDelegate {

    private var _review as ReviewView;

    function initialize(review as ReviewView) {
        Menu2InputDelegate.initialize();
        _review = review;
    }

    function onSelect(item as WatchUi.MenuItem) as Void {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
        _review.handleMenu(item.getId());
    }
}

// Stand-in for showToast: one centered line that pops itself after 1.5 s;
// BACK dismisses it earlier.
(:legacy)
class ToastView extends WatchUi.View {

    private var _text as String;
    private var _timer as Timer.Timer?;

    function initialize(strId as ResourceId) {
        View.initialize();
        _text = WatchUi.loadResource(strId) as String;
    }

    function onShow() as Void {
        _timer = new Timer.Timer();
        _timer.start(method(:dismiss), 1500, false);
    }

    function onHide() as Void {
        if (_timer != null) {
            _timer.stop();
            _timer = null;
        }
    }

    function dismiss() as Void {
        _timer = null;
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onUpdate(dc as Dc) as Void {
        dc.setColor(Theme.PAPER, Theme.BG);
        dc.clear();
        dc.drawText(dc.getWidth() / 2, dc.getHeight() / 2, Graphics.FONT_SMALL,
            _text, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
    }
}
