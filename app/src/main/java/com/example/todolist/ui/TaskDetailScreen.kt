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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.todolist.notification.NotificationReceiver
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextOverflow
import java.io.File
import java.io.FileOutputStream

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

    var title by rememberSaveable { mutableStateOf(task?.title ?: "") }
    var description by rememberSaveable { mutableStateOf(task?.description ?: "") }
    var dueAt by remember { mutableStateOf(task?.dueAt ?: System.currentTimeMillis()) }
    var isCompleted by remember { mutableStateOf(task?.isCompleted ?: false) }
    var notificationEnabled by remember { mutableStateOf(task?.notificationEnabled ?: false) }
    var notificationMinutesInAdvance by remember { mutableStateOf(task?.notificationMinutesInAdvance ?: 0) }
    var category by remember { mutableStateOf(task?.category ?: "") }
    val context = LocalContext.current
    var attachments = remember { mutableStateListOf<String>().apply { task?.attachments?.forEach { add(it.fileUri) } } }
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris?.forEach { uri ->
            val internalPath = copyFileToInternalStorage(context, uri)
            if (internalPath != null) {
                // Dodaj ścieżkę do wewnętrznego pliku, nie URI oryginalnego pliku
                attachments.add("file://$internalPath")
                Toast.makeText(context, "Plik skopiowano do aplikacji", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Nie udało się skopiować pliku", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val categories = listOf("Dom", "Praca", "Szkoła", "Inne")
    val notificationAdvanceOptions = listOf(0, 5, 10, 15, 30, 60) // Opcje w minutach
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedNotificationMinutes by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

//    Spacer(modifier = Modifier.height(16.dp))

    Column(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 40.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {


        // Tytuł
        Box(
            modifier = Modifier
                .padding(8.dp)
                .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tytuł") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Opis
        Box(
            modifier = Modifier
                .padding(8.dp)
                .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Wybór kategorii
        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory },
            modifier = Modifier
                .padding(8.dp)
                .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Kategoria") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) }
            )
            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            category = cat
                            expandedCategory = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Sekcja Zakończone
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { isCompleted = it },
                modifier = Modifier.padding(8.dp),
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                "Zakończone",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sekcja Powiadomienie
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = notificationEnabled,
                onCheckedChange = {
                    notificationEnabled = it
                    if (!it) {
                        dueAt = task?.dueAt ?: System.currentTimeMillis()
                    }
                },
                modifier = Modifier.padding(8.dp),
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = if (notificationEnabled) "Powiadomienie włączone" else "Powiadomienie wyłączone",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Wybór daty i godziny wykonania zadania tylko jeśli powiadomienie jest włączone
        if (notificationEnabled) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth()
            ) {
                DateTimePickerField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    initialDateTime = dueAt,
                    onDateTimeSelected = { selectedDateTime ->
                        dueAt = selectedDateTime
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Wybór minut wyprzedzenia powiadomienia
            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = expandedNotificationMinutes,
                onExpandedChange = { expandedNotificationMinutes = !expandedNotificationMinutes },
                modifier = Modifier
                    .padding(8.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
            ) {
                OutlinedTextField(
                    value = if (notificationMinutesInAdvance == 0) "Brak wyprzedzenia" else "$notificationMinutesInAdvance minut(y)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Wyprzedzenie powiadomienia (minuty)") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNotificationMinutes) }
                )
                ExposedDropdownMenu(
                    expanded = expandedNotificationMinutes,
                    onDismissRequest = { expandedNotificationMinutes = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    notificationAdvanceOptions.forEach { minutes ->
                        DropdownMenuItem(
                            text = { Text(if (minutes == 0) "Brak wyprzedzenia" else "$minutes minut(y)") },
                            onClick = {
                                notificationMinutesInAdvance = minutes
                                expandedNotificationMinutes = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

        }

        // Obsługa załączników
        Box(
            modifier = Modifier
                .padding(8.dp)
                .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Załączniki:", style = MaterialTheme.typography.titleSmall)
                attachments.forEachIndexed { idx, fileUri ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getFileNameFromUri(context, fileUri),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    try {
                                        // Sprawdź czy to wewnętrzny plik (zaczyna się od "file://")
                                        if (fileUri.startsWith("file://")) {
                                            val filePath = fileUri.substring(7) // Usuń "file://" z początku
                                            val file = File(filePath)

                                            if (file.exists()) {
                                                // Określ typ MIME na podstawie rozszerzenia pliku
                                                val mimeType = getMimeType(file.name)

                                                // Utwórz FileProvider URI dla pliku wewnętrznego
                                                val authority = "${context.packageName}.fileprovider"
                                                val fileProviderUri = androidx.core.content.FileProvider.getUriForFile(
                                                    context,
                                                    authority,
                                                    file
                                                )

                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(fileProviderUri, mimeType)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }

                                                // Sprawdź czy istnieje aplikacja, która może obsłużyć ten typ pliku
                                                if (intent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(intent)
                                                } else {
                                                    Toast.makeText(context, "Brak aplikacji do otwarcia tego typu pliku", Toast.LENGTH_LONG).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "Plik nie istnieje: $filePath", Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            // Stara logika dla zewnętrznych URI
                                            val uri = Uri.parse(fileUri)
                                            val mimeType = context.contentResolver.getType(uri)
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(uri, mimeType)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(intent)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "Błąd podczas otwierania pliku: ${e.localizedMessage ?: e.toString()}", Toast.LENGTH_LONG).show()
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
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .shadow(2.dp, shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp))
                ) {
                    Text("Dodaj załącznik", color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (showValidationError) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .shadow(2.dp, shape = RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFE0E0), shape = RoundedCornerShape(12.dp))
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Uzupełnij tytuł, opis i kategorię!",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .padding(8.dp)
                .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || category.isBlank()) {
                        showValidationError = true
                        return@Button
                    } else {
                        showValidationError = false
                    }
                    val newTask = Task(
                        id = task?.id ?: 0L,
                        title = title,
                        description = description,
                        createdAt = task?.createdAt ?: System.currentTimeMillis(),
                        dueAt = dueAt,
                        isCompleted = isCompleted,
                        notificationEnabled = notificationEnabled,
                        notificationMinutesInAdvance = notificationMinutesInAdvance,
                        category = category,
                        attachments = attachments.map { AttachmentItem(fileUri = it) }.toMutableList()
                    )
                    onSave(newTask)
                    if (notificationEnabled) {
                        scheduleTaskNotification(context, newTask)
                    }
                },
                modifier = Modifier
                    .padding(8.dp)
                    .shadow(2.dp, shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Zapisz", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (task != null && onDelete != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .padding(8.dp)
                        .shadow(2.dp, shape = RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp))

//                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Usuń")
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .padding(8.dp)
                    .shadow(2.dp, shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Home, contentDescription = "Strona główna", tint = Color.White)
            }
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Potwierdzenie usunięcia") },
                text = { Text("Czy na pewno chcesz usunąć to zadanie?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        onDelete?.invoke(task!!)
                    }) {
                        Text("Usuń")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
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

fun copyFileToInternalStorage(context: Context, sourceUri: Uri): String? {
    return try {
        val fileName = getFileNameFromUri(context, sourceUri.toString())
        val contentResolver = context.contentResolver
        val destinationFile = File(context.filesDir, "${System.currentTimeMillis()}_$fileName")

        contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                val buffer = ByteArray(4 * 1024) // 4kb buffer
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }

        destinationFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
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
        // Oblicz czas powiadomienia uwzględniając wyprzedzenie w minutach
        val taskDueTime = task.dueAt ?: System.currentTimeMillis()
        val notificationTimeInMillis = taskDueTime - (task.notificationMinutesInAdvance * 60 * 1000)

        // Jeśli czas powiadomienia jest w przeszłości, pokaż powiadomienie natychmiast
        val actualNotificationTime = if (notificationTimeInMillis <= System.currentTimeMillis()) {
            System.currentTimeMillis() + 5000 // 5 sekund od teraz
        } else {
            notificationTimeInMillis
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    actualNotificationTime,
                    pendingIntent
                )
                // Pokaż potwierdzenie z informacją o czasie powiadomienia
                val notificationDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(actualNotificationTime))
                Toast.makeText(context, "Powiadomienie zaplanowane na: $notificationDate", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Brak uprawnień do ustawiania dokładnych alarmów!", Toast.LENGTH_LONG).show()
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                actualNotificationTime,
                pendingIntent
            )
            // Pokaż potwierdzenie z informacją o czasie powiadomienia
            val notificationDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(actualNotificationTime))
            Toast.makeText(context, "Powiadomienie zaplanowane na: $notificationDate", Toast.LENGTH_LONG).show()
        }
    } catch (e: SecurityException) {
        Toast.makeText(context, "Brak uprawnień do ustawiania dokładnych alarmów!", Toast.LENGTH_LONG).show()
    }
}

// Funkcja do określania typu MIME pliku na podstawie jego rozszerzenia
fun getMimeType(fileName: String): String {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "pdf" -> "application/pdf"
        "txt" -> "text/plain"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        else -> "application/octet-stream" // Ogólny typ binarny dla nieznanych rozszerzeń
    }
}
