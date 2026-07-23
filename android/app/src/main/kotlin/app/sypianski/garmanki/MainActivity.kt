package app.sypianski.garmanki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.sypianski.garmanki.ui.MainScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val app = application as App
        // Activity context so the SDK can show its GCM install/upgrade dialogs.
        app.ciq.initialize(this)
        setContent {
            MainScreen(app)
        }
    }
}
