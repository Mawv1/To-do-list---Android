package com.example.todolist.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.provider.OpenableColumns
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.todolist.data.AttachmentItem
import com.example.todolist.data.Task
import java.text.SimpleDateFormat
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
    val context = LocalContext.current
    var attachments = remember { mutableStateListOf<String>().apply { task?.attachments?.forEach { add(it.fileUri) } } }
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris?.let { attachments.addAll(it.map { uri -> uri.toString() }) }
    }

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
        DateTimePickerField(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            initialDateTime = dueAt,
            onDateTimeSelected = { selectedDateTime ->
                dueAt = selectedDateTime
            }
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
            attachments.forEachIndexed { idx, fileUri ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = getFileNameFromUri(context, fileUri), modifier = Modifier.weight(1f))
                    IconButton(onClick = { attachments.removeAt(idx) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń załącznik")
                    }
                }
            }
            Button(onClick = {
                // Otwórz picker plików (dowolny typ)
                filePickerLauncher.launch(arrayOf("image/*", "application/pdf", "text/plain", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "*/*"))
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
                        attachments = attachments.map { AttachmentItem(fileUri = it) }.toMutableList()
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

// Funkcja pomocnicza do pobierania nazwy pliku z String uri
fun getFileNameFromUri(context: Context, fileUri: String): String {
    val uri = Uri.parse(fileUri)
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex >= 0) {
            return it.getString(nameIndex)
        }
    }
    return uri.lastPathSegment ?: "plik"
}
