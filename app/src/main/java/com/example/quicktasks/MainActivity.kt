package com.example.quicktasks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class TaskItem(
    val id: Long,
    val title: String,
    val done: Boolean
)

enum class Screen { TASKS, STATS, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // "Kuukauden Android" -tyylisesti pidetään teema super yksinkertaisena
            MaterialTheme {
                QuickTasksApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTasksApp() {
    var currentScreen by remember { mutableStateOf(Screen.TASKS) }

    // data
    var tasks by remember {
        mutableStateOf(
            listOf(
                TaskItem(1, "Buy milk", false),
                TaskItem(2, "Finish course project", true)
            )
        )
    }

    // settings
    var showMotivation by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            Screen.TASKS -> "QuickTasks"
                            Screen.STATS -> "Stats"
                            Screen.SETTINGS -> "Settings"
                        }
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == Screen.TASKS,
                    onClick = { currentScreen = Screen.TASKS },
                    label = { Text("Tasks") },
                    icon = { Text("📝") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.STATS,
                    onClick = { currentScreen = Screen.STATS },
                    label = { Text("Stats") },
                    icon = { Text("📊") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.SETTINGS,
                    onClick = { currentScreen = Screen.SETTINGS },
                    label = { Text("Settings") },
                    icon = { Text("⚙️") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (currentScreen) {
                Screen.TASKS -> TasksScreen(
                    tasks = tasks,
                    showMotivation = showMotivation,
                    onAddTask = { title ->
                        val newTask = TaskItem(
                            id = System.currentTimeMillis(),
                            title = title,
                            done = false
                        )
                        tasks = listOf(newTask) + tasks
                    },
                    onToggleDone = { id ->
                        tasks = tasks.map {
                            if (it.id == id) it.copy(done = !it.done) else it
                        }
                    },
                    onDelete = { id ->
                        tasks = tasks.filterNot { it.id == id }
                    }
                )

                Screen.STATS -> StatsScreen(tasks = tasks)

                Screen.SETTINGS -> SettingsScreen(
                    showMotivation = showMotivation,
                    onShowMotivationChange = { showMotivation = it }
                )
            }
        }
    }
}

@Composable
fun TasksScreen(
    tasks: List<TaskItem>,
    showMotivation: Boolean,
    onAddTask: (String) -> Unit,
    onToggleDone: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        if (showMotivation) {
            Text(
                text = "Small steps > no steps. ✅",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("New task") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    val trimmed = input.trim()
                    if (trimmed.isNotEmpty()) {
                        onAddTask(trimmed)
                        input = ""
                    }
                }
            ) {
                Text("Add")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (tasks.isEmpty()) {
            Text("No tasks yet. Add one above.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks) { task ->
                    TaskRow(
                        task = task,
                        onToggle = { onToggleDone(task.id) },
                        onDelete = { onDelete(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskRow(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.done,
                onCheckedChange = { onToggle() }
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = task.title,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}

@Composable
fun StatsScreen(tasks: List<TaskItem>) {
    val doneCount = tasks.count { it.done }
    val total = tasks.size
    val percent = if (total == 0) 0 else (doneCount * 100 / total)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Total tasks: $total")
        Text("Done tasks: $doneCount")
        Text("Completion: $percent%")

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = if (total == 0) 0f else doneCount.toFloat() / total.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SettingsScreen(
    showMotivation: Boolean,
    onShowMotivationChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = showMotivation,
                onCheckedChange = onShowMotivationChange
            )
            Spacer(Modifier.width(8.dp))
            Text("Show motivation text")
        }

    }
}
