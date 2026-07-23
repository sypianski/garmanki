import Toybox.Application;
import Toybox.Application.Storage;
import Toybox.Lang;
import Toybox.WatchUi;
import Toybox.Communications;

class GarmankiApp extends Application.AppBase {

    function initialize() {
        AppBase.initialize();
    }

    function onStart(state as Dictionary?) as Void {
        Communications.registerForPhoneAppMessages(Link.get().method(:onMessage));
        // Announce ourselves; a running companion answers with a state push
        // (SCHEMA.md §3) and we flush any queued answers.
        Link.get().hello();
    }

    function onStop(state as Dictionary?) as Void {
    }

    function getInitialView() as [Views] or [Views, InputDelegates] {
        // First run: show the guide (GAR-03); it stamps the flag and
        // switches to HomeView itself.
        if (Storage.getValue("onboardingSeen") != true) {
            return [new OnboardingView(true), new OnboardingDelegate()];
        }
        return [new HomeView(), new HomeDelegate()];
    }
}
