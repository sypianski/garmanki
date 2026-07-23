package app.sypianski.garmanki.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.sypianski.garmanki.App
import app.sypianski.garmanki.R
import app.sypianski.garmanki.data.Settings
import app.sypianski.garmanki.data.SettingsStore
import kotlinx.coroutines.launch

@Composable
fun ShortcutsScreen(app: App, onBack: () -> Unit) {
    val settings by app.settings.flow.collectAsState(initial = null)
    val ankiPermitted = remember { app.anki.available() && app.anki.hasPermission() }

    ScreenScaffold(title = stringResource(R.string.nav_shortcuts_title), onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
        }
    }
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
