import Toybox.Lang;
import Toybox.Application.Storage;

// Event→action mapping for the review screen (GAR-04; decisions D13/D14/D16).
//
// One flat Dictionary keyed by input event, valued by an Anki ease Number
// (1=Again 2=Hard 3=Good 4=Easy) or 0/null for "no grade on this event".
// Persisted under Storage key "actionMap" in exactly the shape the
// companion pushes (SCHEMA.md §8, GAR-07); missing keys fall back to the
// defaults below, so old payloads stay valid.
//
// Gesture keys (tap/swipe*) are defined here but only consumed once the
// touch delegate lands (GAR-05).
module ActionMap {

    const AGAIN = 1;
    const HARD  = 2;
    const GOOD  = 3;
    const EASY  = 4;

    const STORAGE_KEY = "actionMap";

    var _map as Dictionary? = null;

    // D13: DOWN=Good, START=Again · D14: UP=Easy · D15: tap=Good ·
    // D16: swipe →Good ←Again ↑Easy ↓Hard.
    function defaults() as Dictionary {
        return {
            "up"     => EASY,
            "down"   => GOOD,
            "start"  => AGAIN,
            "tap"    => GOOD,
            "swipeR" => GOOD,
            "swipeL" => AGAIN,
            "swipeU" => EASY,
            "swipeD" => HARD,
        };
    }

    function get() as Dictionary {
        if (_map == null) {
            var m = defaults();
            var stored = Storage.getValue(STORAGE_KEY);
            if (stored instanceof Dictionary) {
                var keys = stored.keys();
                for (var i = 0; i < keys.size(); i++) {
                    m[keys[i]] = stored[keys[i]];
                }
            }
            _map = m;
        }
        return _map;
    }

    // Ease for an input event, or null when the event grades nothing.
    // The companion sends 0 for "no grade" (SCHEMA.md §8) — anything
    // outside 1..4 counts as unmapped.
    function easeFor(event as String) as Number? {
        var v = get()[event];
        return (v instanceof Number && v >= AGAIN && v <= EASY) ? v : null;
    }

    // True when some event (button or gesture) already grades this ease —
    // the review menu only lists eases this returns false for (GAR-06).
    function isMapped(ease as Number) as Boolean {
        var m = get();
        var keys = m.keys();
        for (var i = 0; i < keys.size(); i++) {
            var v = m[keys[i]];
            if (v instanceof Number && v == ease) {
                return true;
            }
        }
        return false;
    }

    // Drop the cache after the companion pushed a new table (GAR-07).
    function reload() as Void {
        _map = null;
    }
}
