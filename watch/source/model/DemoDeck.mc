import Toybox.Lang;
import Toybox.Application.Storage;

// Built-in sample deck (TASK-40): 20 Esperanto → English cards that work
// before the phone has pushed anything. It lives outside the pushed state:
// deck index -1 never collides with companion indices, answers and card
// actions are never queued for AnkiDroid, and CardStore.replaceAll leaves
// it alone. Removable from the deck menu; the choice persists under
// Storage "demoRemoved".
module DemoDeck {

    const IDX = -1;
    const MENU_ID = "demo";
    const REMOVE_ID = "demoRemove";

    // Includes ĉ ĝ ĥ ĵ ŝ ŭ on purpose — they check the watch font too.
    const WORDS = [
        ["saluton", "hello"], ["dankon", "thank you"],
        ["akvo", "water"], ["pano", "bread"],
        ["domo", "house"], ["hundo", "dog"],
        ["kato", "cat"], ["libro", "book"],
        ["amiko", "friend"], ["tago", "day"],
        ["nokto", "night"], ["suno", "sun"],
        ["urbo", "city"], ["manĝi", "to eat"],
        ["trinki", "to drink"], ["ĉevalo", "horse"],
        ["ŝipo", "ship"], ["ĵurnalo", "newspaper"],
        ["ĥoro", "choir"], ["aŭto", "car"]
    ];

    function available() as Boolean {
        var removed = Storage.getValue("demoRemoved");
        return !(removed instanceof Boolean && removed);
    }

    function remove() as Void {
        Storage.setValue("demoRemoved", true);
    }

    function isDemo(card as Array) as Boolean {
        return IDX.equals(card[3] as Object?);
    }

    // §2 records; ids are local strings, intervals unknown.
    function cards() as Array {
        var out = [];
        for (var i = 0; i < WORDS.size(); i++) {
            var w = WORDS[i] as Array;
            var id = "demo" + i.toString();
            out.add([id, id, 0, IDX, w[0], w[1], ["", "", "", ""]]);
        }
        return out;
    }
}
