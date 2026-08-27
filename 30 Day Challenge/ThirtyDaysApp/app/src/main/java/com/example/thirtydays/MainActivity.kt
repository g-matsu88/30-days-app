package com.example.thirtydays

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thirtydays.data.DataSource
import com.example.thirtydays.model.Day
import com.example.thirtydays.ui.theme.ThirtyDaysTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThirtyDaysTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChallengeApp()
                }
            }
        }
    }
}

/** Which subset of days is currently shown. */
enum class DayFilter(val label: String) {
    ALL("All"),
    COMPLETED("Completed"),
    INCOMPLETE("Incomplete")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeApp() {
    // Source of truth for all 30 days, backed by a SnapshotStateList so toggling
    // completion recomposes only the affected card.
    val days = remember { DataSource.days.toMutableStateList() }

    // Q1: only one card may be expanded at a time -> a single nullable id, not a
    // per-card boolean, is the state that guarantees this.
    var expandedDayNumber by rememberSaveable { mutableStateOf<Int?>(null) }

    // Q3 (bonus): search text + filter chip selection.
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(DayFilter.ALL) }

    val completedCount = days.count { it.isCompleted }

    // Derived, filtered list. Recomputed from `days` on every recomposition using
    // plain Kotlin collection operations (filter), as requested.
    val visibleDays = days
        .filter { day ->
            when (selectedFilter) {
                DayFilter.ALL -> true
                DayFilter.COMPLETED -> day.isCompleted
                DayFilter.INCOMPLETE -> !day.isCompleted
            }
        }
        .filter { day ->
            searchQuery.isBlank() ||
                day.title.contains(searchQuery, ignoreCase = true) ||
                "day ${day.dayNumber}".contains(searchQuery, ignoreCase = true)
        }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text(text = "30-Day Challenge", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                ProgressHeader(completed = completedCount, total = days.size)
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                FilterChipRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    ) { innerPadding ->
        DayList(
            days = visibleDays,
            expandedDayNumber = expandedDayNumber,
            onExpandToggle = { day ->
                // Q1: clicking the already-open card collapses it; clicking any other
                // card collapses whatever was open and expands the new one.
                expandedDayNumber = if (expandedDayNumber == day.dayNumber) null else day.dayNumber
            },
            onCheckedChange = { day, checked ->
                val index = days.indexOfFirst { it.dayNumber == day.dayNumber }
                if (index != -1) {
                    days[index] = day.copy(isCompleted = checked)
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun ProgressHeader(completed: Int, total: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val progress = if (total > 0) completed.toFloat() / total else 0f
        Text(
            text = "Progress: $completed / $total Completed",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search days...") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipRow(
    selectedFilter: DayFilter,
    onFilterSelected: (DayFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DayFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) }
            )
        }
    }
}

@Composable
fun DayList(
    days: List<Day>,
    expandedDayNumber: Int?,
    onExpandToggle: (Day) -> Unit,
    onCheckedChange: (Day, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No days match your search/filter.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(items = days, key = { it.dayNumber }) { day ->
            DayCard(
                day = day,
                expanded = day.dayNumber == expandedDayNumber,
                onExpandToggle = { onExpandToggle(day) },
                onCheckedChange = { checked -> onCheckedChange(day, checked) }
            )
        }
    }
}

@Composable
fun DayCard(
    day: Day,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onExpandToggle,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 200)),
        colors = CardDefaults.cardColors(
            containerColor = if (day.isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Clicking the checkbox only toggles completion; it does not bubble
                // up and trigger the card's own onClick (expand/collapse).
                Checkbox(
                    checked = day.isCompleted,
                    onCheckedChange = onCheckedChange
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Day ${day.dayNumber}: ${day.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = day.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
