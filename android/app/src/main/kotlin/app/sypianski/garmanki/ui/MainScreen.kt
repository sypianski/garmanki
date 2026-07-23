package app.sypianski.garmanki.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.sypianski.garmanki.App
import app.sypianski.garmanki.BuildConfig
import app.sypianski.garmanki.R
import app.sypianski.garmanki.anki.FlashCards
import app.sypianski.garmanki.ciq.CiqState
import app.sypianski.garmanki.ciq.PushStatus
import app.sypianski.garmanki.ciq.WatchDevice
import app.sypianski.garmanki.data.Settings
import app.sypianski.garmanki.data.SettingsStore
import app.sypianski.garmanki.data.Stats
import kotlinx.coroutines.launch

private const val ROUTE_HOME = "home"
private const val ROUTE_DECKS = "decks"
private const val ROUTE_SHORTCUTS = "shortcuts"

@Composable
fun MainScreen(app: App) {
    val settings by app.settings.flow.collectAsState(initial = null)
    GarmankiTheme(eink = settings?.themeMode == SettingsStore.THEME_EINK) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = ROUTE_HOME) {
            composable(ROUTE_HOME) { HomeScreen(app, navController) }
            composable(ROUTE_DECKS) {
                DecksScreen(app, onBack = { navController.popBackStack() })
            }
            composable(ROUTE_SHORTCUTS) {
                ShortcutsScreen(app, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun HomeScreen(app: App, navController: NavHostController) {
    val context = LocalContext.current
    val ciqState by app.ciq.state.collectAsState()
    val devices by app.ciq.devices.collectAsState()
    val pushStatus by app.ciq.pushStatus.collectAsState()
    val engineUi by app.engine.ui.collectAsState()
    val settings by app.settings.flow.collectAsState(initial = null)

    var refreshTick by remember { mutableIntStateOf(0) }
    val ankiAvailable = remember(refreshTick) { app.anki.available() }
    val ankiPermitted = remember(refreshTick) { ankiAvailable && app.anki.hasPermission() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshTick++ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Masthead() }
            item { WatchCard(ciqState, devices) }
            item { AnkiCard(ankiAvailable, ankiPermitted, settings, permissionLauncher) }

            if (ankiPermitted) {
                item {
                    LinkRow(
                        label = stringResource(R.string.decks_section),
                        onClick = { navController.navigate(ROUTE_DECKS) },
                    )
                }
            }

            item {
                LinkRow(
                    label = stringResource(R.string.controls_section),
                    onClick = { navController.navigate(ROUTE_SHORTCUTS) },
                )
            }

            if (ankiPermitted) {
                item {
                    PushCard(
                        pushBusy = engineUi.busy,
                        canPush = app.ciq.canPush(),
                        pushStatus = pushStatus,
                        skipped = engineUi.skipped,
                        lastApplied = engineUi.lastApplied,
                        onPush = { app.appScope.launch { app.engine.pushState() } },
                    )
                }
            }

            item {
                ThemeCard(
                    current = settings?.themeMode ?: SettingsStore.THEME_SYSTEM,
                    onSelect = { mode -> app.appScope.launch { app.settings.setThemeMode(mode) } },
                )
            }

            item {
                CustomStudyFooter {
                    context.packageManager
                        .getLaunchIntentForPackage(FlashCards.ANKIDROID_PACKAGE)
                        ?.let { context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScreenScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    GarmankiTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.nav_back))
                        }
                    },
                )
            },
        ) { padding -> content(padding) }
    }
}

@Composable
private fun ThemeCard(current: String, onSelect: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.theme_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeOption(
                    label = stringResource(R.string.theme_dynamic),
                    selected = current != SettingsStore.THEME_EINK,
                    onClick = { onSelect(SettingsStore.THEME_SYSTEM) },
                    modifier = Modifier.weight(1f),
                )
                ThemeOption(
                    label = stringResource(R.string.theme_eink),
                    selected = current == SettingsStore.THEME_EINK,
                    onClick = { onSelect(SettingsStore.THEME_EINK) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Filled = selected, outlined = not — a segmented look on stable M3 buttons. */
@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
    }
}

@Composable
private fun LinkRow(label: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Masthead() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
    }
}

@Composable
internal fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}

@Composable
private fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(color, CircleShape),
    )
}

/** Technical read-out (push/sync progress) — the one place Space Mono remains. */
@Composable
internal fun LogLine(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = MonoFamily),
        color = color,
    )
}

@Composable
private fun WatchCard(state: CiqState, devices: List<WatchDevice>) {
    val connectedStr = stringResource(R.string.watch_connected)
    val disconnectedStr = stringResource(R.string.watch_disconnected)
    val appMissingStr = stringResource(R.string.watch_app_missing)
    SectionCard(stringResource(R.string.watch_section)) {
        when (val s = state) {
            is CiqState.Error -> Text(
                stringResource(R.string.watch_sdk_error, s.message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            CiqState.Initializing, CiqState.Uninitialized -> Text(
                stringResource(R.string.watch_initializing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CiqState.Ready -> {
                if (devices.isEmpty()) {
                    Text(
                        stringResource(R.string.watch_no_devices),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    devices.forEach { d ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            StatusDot(
                                if (d.connected) gradeColor(3)
                                else MaterialTheme.colorScheme.outline,
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    d.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    if (d.connected) connectedStr else disconnectedStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (d.appInstalled == false) {
                            Text(
                                appMissingStr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnkiCard(
    ankiAvailable: Boolean,
    ankiPermitted: Boolean,
    settings: Settings?,
    permissionLauncher: ActivityResultLauncher<String>,
) {
    SectionCard(stringResource(R.string.anki_section)) {
        when {
            !ankiAvailable -> Text(
                stringResource(R.string.anki_missing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            !ankiPermitted -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.anki_no_permission),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = { permissionLauncher.launch(FlashCards.READ_WRITE_PERMISSION) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.anki_grant))
                }
            }
            else -> {
                val today = System.currentTimeMillis() / 86_400_000L
                val log = settings?.replayLog ?: emptyMap()
                val done = Stats.doneToday(log, today)
                val streak = Stats.streak(log, today)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "$done cards",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Streak",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { (streak.coerceAtMost(14)) / 14f },
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "$streak d",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun PushCard(
    pushBusy: Boolean,
    canPush: Boolean,
    pushStatus: PushStatus,
    skipped: Int,
    lastApplied: Pair<Int, Int>?,
    onPush: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = onPush,
                enabled = !pushBusy && canPush,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.send_to_watch))
            }
            PushStatusLog(pushStatus)
            if (skipped > 0) {
                LogLine(
                    stringResource(R.string.push_skipped_cards, skipped),
                    MaterialTheme.colorScheme.tertiary,
                )
            }
            lastApplied?.let { (applied, stale) ->
                LogLine(stringResource(R.string.answers_applied, applied, stale))
            }
        }
    }
}

@Composable
private fun PushStatusLog(status: PushStatus) {
    val text: String?
    val color: Color
    when (status) {
        PushStatus.Idle -> {
            text = null; color = MaterialTheme.colorScheme.onSurfaceVariant
        }
        is PushStatus.Sending -> {
            text = stringResource(R.string.push_sending, status.seq, status.of)
            color = MaterialTheme.colorScheme.onSurfaceVariant
        }
        PushStatus.AwaitingAck -> {
            text = stringResource(R.string.push_waiting_ack)
            color = MaterialTheme.colorScheme.onSurfaceVariant
        }
        is PushStatus.Done -> {
            text = stringResource(R.string.push_done, status.rev)
            color = gradeColor(3)
        }
        is PushStatus.Failed -> {
            text = stringResource(
                R.string.push_failed,
                status.reason.name + (status.detail?.let { ": $it" } ?: ""),
            )
            color = MaterialTheme.colorScheme.error
        }
    }
    if (text != null) LogLine(text, color)
}

@Composable
private fun CustomStudyFooter(onOpen: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            stringResource(R.string.custom_study_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.open_ankidroid))
        }
    }
}
