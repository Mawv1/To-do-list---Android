package com.example.todolist.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolist.data.Task
import java.util.*

@Composable
fun TaskDetailScreen(
    task: Task?,
    onSave: (Task) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var dueAt by remember { mutableStateOf(task?.dueAt ?: System.currentTimeMillis()) }
    var isCompleted by remember { mutableStateOf(task?.isCompleted ?: false) }
    var notificationEnabled by remember { mutableStateOf(task?.notificationEnabled ?: false) }
    var category by remember { mutableStateOf(task?.category ?: "") }
    var attachments = remember { mutableStateListOf<String>().apply { task?.attachments?.let { addAll(it) } } }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Tytuł") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Opis") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { isCompleted = it }
            )
            Text("Zakończone")
            Spacer(modifier = Modifier.width(16.dp))
            Checkbox(
                checked = notificationEnabled,
                onCheckedChange = { notificationEnabled = it }
            )
            Text("Powiadomienie")
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Wybór daty i godziny wykonania zadania
        OutlinedTextField(
            value = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(dueAt)),
            onValueChange = {
                // Prosta obsługa wpisywania daty/godziny jako tekst (do rozbudowy na DatePicker)
                try {
                    dueAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").parse(it)?.time ?: dueAt
                } catch (_: Exception) {}
            },
            label = { Text("Termin (rrrr-mm-dd gg:mm)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Kategoria") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Obsługa załączników (lista nazw plików i przycisk dodawania)
        Text("Załączniki:", style = MaterialTheme.typography.titleSmall)
        Column(modifier = Modifier.fillMaxWidth()) {
            attachments.forEachIndexed { idx, attachment ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = attachment, modifier = Modifier.weight(1f))
                    IconButton(onClick = { attachments.removeAt(idx) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń załącznik")
                    }
                }
            }
            Button(onClick = {
                // Przykładowe dodanie załącznika (do rozbudowy o picker plików)
                attachments.add("nowy_załącznik_${attachments.size + 1}.txt")
            }) {
                Text("Dodaj załącznik")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = {
                onSave(
                    Task(
                        id = task?.id ?: 0L,
                        title = title,
                        description = description,
                        createdAt = task?.createdAt ?: System.currentTimeMillis(),
                        dueAt = dueAt,
                        isCompleted = isCompleted,
                        notificationEnabled = notificationEnabled,
                        category = category,
                        attachments = attachments.toList()
                    )
                )
            }) {
                Text("Zapisz")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onCancel) {
                Text("Anuluj")
            }
        }
    }
}

