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
        menu.addItem(new WatchUi.MenuItem(
            WatchUi.loadResource(Rez.Strings.MenuSyncTitle) as String,
            null, "sync", null));
        // Re-run the first-launch guide on demand (GAR-03).
        menu.addItem(new WatchUi.MenuItem(
            WatchUi.loadResource(Rez.Strings.GuideTitle) as String,
            null, "guide", null));
        WatchUi.pushView(menu, new DeckMenuDelegate(), WatchUi.SLIDE_LEFT);
    }
}

class DeckMenuDelegate extends WatchUi.Menu2InputDelegate {

    function initialize() {
        Menu2InputDelegate.initialize();
    }

    function onSelect(item as WatchUi.MenuItem) as Void {
        var deckIdx = item.getId();
        if ("sync".equals(deckIdx)) {
            WatchUi.popView(WatchUi.SLIDE_RIGHT);
            Link.get().hello();
            Link.get().setStatus(WatchUi.loadResource(Rez.Strings.SyncSent) as String);
            return;
        }
        if ("guide".equals(deckIdx)) {
            WatchUi.pushView(new OnboardingView(false), new OnboardingDelegate(),
                WatchUi.SLIDE_LEFT);
            return;
        }
        if (!(deckIdx instanceof Number)) {
            return;
        }
        var cards = CardStore.cardsForDeck(deckIdx);
        if (cards.size() == 0) {
            WatchUi.showToast(Rez.Strings.DeckEmpty, null);
            return;
        }
        var decks = CardStore.getDecks();
        var name = "";
        for (var i = 0; i < decks.size(); i++) {
            if (decks[i][0] == deckIdx) {
                name = decks[i][2];
            }
        }
        WatchUi.pushView(new ReviewView(cards, name),
            new ReviewDelegate(), WatchUi.SLIDE_LEFT);
    }

    function onBack() as Void {
        WatchUi.popView(WatchUi.SLIDE_RIGHT);
    }
}
