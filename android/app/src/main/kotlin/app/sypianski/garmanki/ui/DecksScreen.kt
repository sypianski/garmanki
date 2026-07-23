package app.sypianski.garmanki.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.sypianski.garmanki.App
import app.sypianski.garmanki.R
import app.sypianski.garmanki.anki.AnkiDeck
import app.sypianski.garmanki.anki.DeckNode
import app.sypianski.garmanki.anki.allDeckIds
import app.sypianski.garmanki.anki.buildDeckTree
import app.sypianski.garmanki.data.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DecksScreen(app: App, onBack: () -> Unit) {
    val settings by app.settings.flow.collectAsState(initial = null)
    var decks by remember { mutableStateOf<List<AnkiDeck>?>(null) }

    LaunchedEffect(Unit) {
        decks = withContext(Dispatchers.IO) {
            runCatching { app.anki.listDecks() }.getOrDefault(emptyList())
        }
    }

    ScreenScaffold(title = stringResource(R.string.nav_decks_title), onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
