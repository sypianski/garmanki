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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.sypianski.garmanki.App
import app.sypianski.garmanki.BuildConfig
import app.sypianski.garmanki.R
import app.sypianski.garmanki.anki.AnkiDeck
import app.sypianski.garmanki.anki.DeckNode
import app.sypianski.garmanki.anki.FlashCards
import app.sypianski.garmanki.anki.allDeckIds
import app.sypianski.garmanki.anki.buildDeckTree
import app.sypianski.garmanki.ciq.CiqState
import app.sypianski.garmanki.ciq.PushStatus
import app.sypianski.garmanki.ciq.WatchDevice
import app.sypianski.garmanki.data.Settings
import app.sypianski.garmanki.data.SettingsStore
import app.sypianski.garmanki.data.Stats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MainScreen(app: App) {
    val context = LocalContext.current
    val ciqState by app.ciq.state.collectAsState()
    val devices by app.ciq.devices.collectAsState()
    val pushStatus by app.ciq.pushStatus.collectAsState()
    val engineUi by app.engine.ui.collectAsState()
    val settings by app.settings.flow.collectAsState(initial = null)

    var refreshTick by remember { mutableIntStateOf(0) }
    var decks by remember { mutableStateOf<List<AnkiDeck>?>(null) }
    val ankiAvailable = remember(refreshTick) { app.anki.available() }
    val ankiPermitted = remember(refreshTick) { ankiAvailable && app.anki.hasPermission() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshTick++ }

    LaunchedEffect(refreshTick, ankiPermitted) {
        if (ankiPermitted) {
            decks = withContext(Dispatchers.IO) {
                runCatching { app.anki.listDecks() }.getOrDefault(emptyList())
            }
        }
    }

    GarmankiTheme {
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
                        DecksCard(
                            decks = decks,
                            settings = settings,
                            limit = settings?.cardLimit ?: 100,
                            onToggleDecks = { ids, checked ->
                                val current = settings?.selectedDecks ?: emptySet()
                                val next = if (checked) current + ids else current - ids
                                app.appScope.launch { app.settings.setSelectedDecks(next) }
                            },
                            onLimit = { v ->
                                app.appScope.launch { app.settings.setCardLimit(v) }
                            },
                        )
                    }

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
                    settings?.let { s ->
                        WatchControlsCard(
                            settings = s,
                            onSaveMapping = { m ->
                                app.appScope.launch {
                                    app.settings.setActionMap(m)
                                    if (ankiPermitted) app.engine.pushState()
                                }
                            },
                            onCardActions = { a ->
                                app.appScope.launch { app.settings.setCardActions(a) }
                            },
                            onGuideAgain = {
                                app.appScope.launch {
                                    app.settings.bumpGuideReset()
                                    if (ankiPermitted) app.engine.pushState()
                                }
                            },
                        )
                    }
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
private fun SectionCard(
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
private fun LogLine(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
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
private fun DecksCard(
    decks: List<AnkiDeck>?,
    settings: Settings?,
    limit: Int,
    onToggleDecks: (List<String>, Boolean) -> Unit,
    onLimit: (Int) -> Unit,
) {
    SectionCard(stringResource(R.string.decks_section)) {
        when {
            decks == null -> Unit
            decks.isEmpty() -> Text(
                stringResource(R.string.decks_none),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> {
                val tree = remember(decks) { buildDeckTree(decks) }
                val expanded = remember { mutableStateMapOf<String, Boolean>() }
                val allIds = remember(decks) { decks.map { it.id.toString() } }
                val selectedDecks = settings?.selectedDecks ?: emptySet()
                val allState = when {
                    allIds.isEmpty() -> ToggleableState.Off
                    allIds.all { it in selectedDecks } -> ToggleableState.On
                    allIds.none { it in selectedDecks } -> ToggleableState.Off
                    else -> ToggleableState.Indeterminate
                }
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleDecks(allIds, allState != ToggleableState.On) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.decks_select_all),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        TriStateCheckbox(
                            state = allState,
                            onClick = { onToggleDecks(allIds, allState != ToggleableState.On) },
                        )
                    }
                    HorizontalDivider()
                    DeckTreeRows(
                        nodes = tree,
                        depth = 0,
                        selectedDecks = selectedDecks,
                        expanded = expanded,
                        onToggleDecks = onToggleDecks,
                    )
                }
            }
        }
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Cards per deck",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                limit.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = limit.toFloat(),
            valueRange = 10f..100f,
            onValueChange = { onLimit(it.toInt()) },
        )
    }
}

@Composable
private fun DeckTreeRows(
    nodes: List<DeckNode>,
    depth: Int,
    selectedDecks: Set<String>,
    expanded: SnapshotStateMap<String, Boolean>,
    onToggleDecks: (List<String>, Boolean) -> Unit,
) {
    nodes.forEach { node ->
        val indent = (depth * 20).dp
        if (node.children.isNotEmpty()) {
            val isExpanded = expanded[node.fullName] ?: true
            val groupIds = remember(node) { node.allDeckIds() }
            val groupState = when {
                groupIds.isEmpty() -> ToggleableState.Off
                groupIds.all { it in selectedDecks } -> ToggleableState.On
                groupIds.none { it in selectedDecks } -> ToggleableState.Off
                else -> ToggleableState.Indeterminate
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded[node.fullName] = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (isExpanded) "▾" else "▸",
                    modifier = Modifier.padding(start = indent, end = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                    Text(
                        node.segment,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    node.deck?.let { DeckDueRow(it) }
                }
                TriStateCheckbox(
                    state = groupState,
                    onClick = { onToggleDecks(groupIds, groupState != ToggleableState.On) },
                )
            }
            if (isExpanded) {
                DeckTreeRows(node.children, depth + 1, selectedDecks, expanded, onToggleDecks)
            }
        } else {
            val deck = node.deck ?: return@forEach
            val id = deck.id.toString()
            val selected = id in selectedDecks
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDecks(listOf(id), !selected) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(start = indent)
                        .padding(vertical = 6.dp),
                ) {
                    Text(
                        node.segment,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    DeckDueRow(deck)
                }
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onToggleDecks(listOf(id), it) },
                )
            }
        }
    }
}

@Composable
private fun DeckDueRow(deck: AnkiDeck) {
    Row(
        modifier = Modifier.padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DueChip("new", deck.new, MaterialTheme.colorScheme.primary)
        DueChip("learn", deck.learn, MaterialTheme.colorScheme.tertiary)
        DueChip("review", deck.review, MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DueChip(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            count.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
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

/** Watch input events → UI labels, in display order (SCHEMA.md §8). */
private val EVENT_LABELS = listOf(
    "down" to "Button lower-left",
    "start" to "Button upper-right",
    "up" to "Button mid-left",
    "tap" to "Tap",
    "swipeR" to "Swipe right",
    "swipeL" to "Swipe left",
    "swipeU" to "Swipe up",
    "swipeD" to "Swipe down",
)

private val CARD_ACTION_LABELS = listOf(
    "susp" to "Suspend",
    "bury" to "Bury",
    "flag" to "Flag",
    "del" to "Delete",
)

private fun easeLabel(ease: Int): String = when (ease) {
    1 -> "Again"
    2 -> "Hard"
    3 -> "Good"
    4 -> "Easy"
    else -> "—"
}

@Composable
private fun WatchControlsCard(
    settings: Settings,
    onSaveMapping: (Map<String, Int>) -> Unit,
    onCardActions: (Set<String>) -> Unit,
    onGuideAgain: () -> Unit,
) {
    // Draft resets whenever the persisted map changes (i.e. after save).
    var draft by remember(settings.actionMap) { mutableStateOf(settings.actionMap) }
    val dirty = draft != settings.actionMap
    val valid = draft.containsValue(1) && draft.containsValue(3)

    SectionCard(stringResource(R.string.controls_section)) {
        EVENT_LABELS.forEach { (event, label) ->
            MappingRow(
                label = label,
                value = draft[event] ?: 0,
                onChange = { draft = draft + (event to it) },
            )
        }
        if (!valid) {
            Text(
                stringResource(R.string.controls_need_grades),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { onSaveMapping(draft) },
                enabled = dirty && valid,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.controls_save))
            }
            OutlinedButton(
                onClick = { draft = SettingsStore.DEFAULT_ACTION_MAP },
                enabled = draft != SettingsStore.DEFAULT_ACTION_MAP,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.controls_reset))
            }
        }
        HorizontalDivider()
        Text(
            stringResource(R.string.controls_actions_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CARD_ACTION_LABELS.forEach { (code, label) ->
            val enabled = settings.cardActions.contains(code)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = { checked ->
                        onCardActions(
                            if (checked) settings.cardActions + code
                            else settings.cardActions - code,
                        )
                    },
                )
            }
        }
        TextButton(onClick = onGuideAgain) {
            Text(stringResource(R.string.controls_guide))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MappingRow(label: String, value: Int, onChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = easeLabel(value),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            textStyle = LocalTextStyle.current.copy(
                color = gradeColor(value),
                fontWeight = FontWeight.Medium,
            ),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            listOf(1, 2, 3, 4, 0).forEach { ease ->
                DropdownMenuItem(
                    text = { Text(easeLabel(ease), color = gradeColor(ease)) },
                    onClick = {
                        onChange(ease)
                        expanded = false
                    },
                )
            }
        }
    }
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
