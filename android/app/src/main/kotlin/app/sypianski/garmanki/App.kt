package app.sypianski.garmanki

import android.app.Application
import app.sypianski.garmanki.anki.AnkiDroidClient
import app.sypianski.garmanki.ciq.CiqManager
import app.sypianski.garmanki.data.SettingsStore
import app.sypianski.garmanki.sync.SyncEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Manual singletons — no DI framework (house pattern from notes_and_codes). */
class App : Application() {

    lateinit var settings: SettingsStore
        private set
    lateinit var anki: AnkiDroidClient
        private set
    lateinit var ciq: CiqManager
        private set
    lateinit var engine: SyncEngine
        private set

    /**
     * Process-lifetime scope: pushes and answer replays must survive the
     * user navigating away mid-operation (watch ACK timeout is 30 s).
     */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        settings = SettingsStore(this)
        anki = AnkiDroidClient(this)
        ciq = CiqManager(this)
        engine = SyncEngine(anki, ciq, settings, appScope)
        engine.start()
    }
}
