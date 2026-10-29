import Toybox.Lang;
import Toybox.WatchUi;

// Deck picker — built fresh on every push so counts are current.
module DeckMenu {

    function push() as Void {
        var menu = new WatchUi.Menu2({:title => Rez.Strings.MenuDecksTitle});
        var decks = CardStore.getDecks();
        for (var i = 0; i < decks.size(); i++) {
            var d = decks[i]; // [deckIdx, deckId, name, nNew, nLrn, nRev]
            var remaining = CardStore.cardsForDeck(d[0]).size();
            var sub = remaining > 0
                ? remaining.toString() + " " + (WatchUi.loadResource(Rez.Strings.HomeDue) as String)
                : WatchUi.loadResource(Rez.Strings.DeckEmpty) as String;
            menu.addItem(new WatchUi.MenuItem(d[2], sub, d[0], null));
        }
        if (DemoDeck.available()) {
            menu.addItem(new WatchUi.MenuItem(
                WatchUi.loadResource(Rez.Strings.DemoDeckName) as String,
                WatchUi.loadResource(Rez.Strings.DemoDeckSub) as String,
                DemoDeck.MENU_ID, null));
        }
        menu.addItem(new WatchUi.MenuItem(
            WatchUi.loadResource(Rez.Strings.MenuSyncTitle) as String,
            null, "sync", null));
        // Re-run the first-launch guide on demand (GAR-03).
        menu.addItem(new WatchUi.MenuItem(
            WatchUi.loadResource(Rez.Strings.GuideTitle) as String,
            null, "guide", null));
        if (DemoDeck.available()) {
            menu.addItem(new WatchUi.MenuItem(
                WatchUi.loadResource(Rez.Strings.DemoRemove) as String,
                null, DemoDeck.REMOVE_ID, null));
        }
        WatchUi.pushView(menu, new DeckMenuDelegate(menu), WatchUi.SLIDE_LEFT);
    }
}

class DeckMenuDelegate extends WatchUi.Menu2InputDelegate {

    private var _menu as WatchUi.Menu2;

    function initialize(menu as WatchUi.Menu2) {
        Menu2InputDelegate.initialize();
        _menu = menu;
    }

    function onSelect(item as WatchUi.MenuItem) as Void {
        var deckIdx = item.getId();
        if ("sync".equals(deckIdx)) {
            WatchUi.popView(WatchUi.SLIDE_RIGHT);
            Link.get().hello();
            Link.get().setStatus(WatchUi.loadResource(Rez.Strings.SyncSent) as String);
            return;
        }
        if (DemoDeck.MENU_ID.equals(deckIdx)) {
            var demo = new ReviewView(DemoDeck.cards(),
                WatchUi.loadResource(Rez.Strings.DemoDeckName) as String);
            WatchUi.pushView(demo, new ReviewDelegate(demo), WatchUi.SLIDE_LEFT);
            return;
        }
        if (DemoDeck.REMOVE_ID.equals(deckIdx)) {
            WatchUi.pushView(
                new WatchUi.Confirmation(WatchUi.loadResource(Rez.Strings.DemoRemoveAsk) as String),
                new DemoRemoveDelegate(_menu), WatchUi.SLIDE_IMMEDIATE);
            return;
        }
        if ("guide".equals(deckIdx)) {
            var guide = new OnboardingView(false);
            WatchUi.pushView(guide, new OnboardingDelegate(guide), WatchUi.SLIDE_LEFT);
            return;
        }
        if (!(deckIdx instanceof Number)) {
            return;
        }
        var cards = CardStore.cardsForDeck(deckIdx);
        if (cards.size() == 0) {
            Compat.toast(Rez.Strings.DeckEmpty);
            return;
        }
        var decks = CardStore.getDecks();
        var name = "";
        for (var i = 0; i < decks.size(); i++) {
            if (decks[i][0] == deckIdx) {
                name = decks[i][2];
            }
        }
        var review = new ReviewView(cards, name);
        WatchUi.pushView(review, new ReviewDelegate(review), WatchUi.SLIDE_LEFT);
    }

    function onBack() as Void {
        WatchUi.popView(WatchUi.SLIDE_RIGHT);
    }
}

// Drops the sample deck and its two menu rows in place (TASK-40).
class DemoRemoveDelegate extends WatchUi.ConfirmationDelegate {

    private var _menu as WatchUi.Menu2;

    function initialize(menu as WatchUi.Menu2) {
        ConfirmationDelegate.initialize();
        _menu = menu;
    }

    function onResponse(response as WatchUi.Confirm) as Boolean {
        if (response == WatchUi.CONFIRM_YES) {
            DemoDeck.remove();
            var ids = [DemoDeck.MENU_ID, DemoDeck.REMOVE_ID];
            for (var i = 0; i < ids.size(); i++) {
                var idx = _menu.findItemById(ids[i]);
                if (idx >= 0) {
                    _menu.deleteItem(idx);
                }
            }
        }
        return true;
    }
}
