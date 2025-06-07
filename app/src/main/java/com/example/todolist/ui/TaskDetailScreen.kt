package com.example.todolist.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import com.example.todolist.notification.NotificationReceiver

@Composable
fun TaskDetailScreen(
    task: Task?,
    onSave: (Task) -> Unit,
    onCancel: () -> Unit,
    onDelete: ((Task) -> Unit)? = null
) {
    // Wyświetl komunikat o braku zadania TYLKO jeśli próbujemy edytować (onDelete != null)
    if (task == null && onDelete != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Nie znaleziono zadania", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onCancel) {
                Text("Powrót")
            }
        }
        return
    }

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
                    Text(
                        text = getFileNameFromUri(context, fileUri),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(fileUri), context.contentResolver.getType(Uri.parse(fileUri)))
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Nie można otworzyć pliku. Być może został usunięty z urządzenia.", Toast.LENGTH_LONG).show()
                                }
                            }
                    )
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
                val newTask = Task(
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
                onSave(newTask)
                if (notificationEnabled) {
                    scheduleTaskNotification(context, newTask)
                }
            }) {
                Text("Zapisz")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onCancel) {
                Text("Anuluj")
            }
            if (task != null && onDelete != null) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = { onDelete(task) }) {
                    Text("Usuń")
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onCancel) {
                Text("Strona główna")
            }
        }
    }
}

// Funkcja pomocnicza do pobierania nazwy pliku z String uri
fun getFileNameFromUri(context: Context, fileUri: String): String {
    return try {
        val uri = Uri.parse(fileUri)
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                return it.getString(nameIndex)
            }
        }
        uri.lastPathSegment ?: "plik"
    } catch (e: SecurityException) {
        "Plik niedostępny"
    } catch (e: Exception) {
        "Plik niedostępny"
    }
}

fun scheduleTaskNotification(context: Context, task: Task) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("taskId", task.id)
        putExtra("title", task.title)
        putExtra("description", task.description)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        task.id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.dueAt ?: System.currentTimeMillis(),
                    pendingIntent
                )
            } else {
                Toast.makeText(context, "Brak uprawnień do ustawiania dokładnych alarmów!", Toast.LENGTH_LONG).show()
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                task.dueAt ?: System.currentTimeMillis(),
                pendingIntent
            )
        }
    } catch (e: SecurityException) {
        Toast.makeText(context, "Brak uprawnień do ustawiania dokładnych alarmów!", Toast.LENGTH_LONG).show()
    }
}
