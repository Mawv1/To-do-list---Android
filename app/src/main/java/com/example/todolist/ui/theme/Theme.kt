package com.example.todolist.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Yellow40, // Akcent do przycisków, FAB itp.
    secondary = Orange40, // Dodatkowy akcent
    tertiary = Blue80, // Drobne akcenty
    background = Color(0xFF232323), // Ciemne, ale nie czarne tło
    surface = Color(0xFF2C2C2C),
    onPrimary = Color(0xFF232323), // Ciemny tekst na żółtym
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFFFF9C4), // Jasny tekst na ciemnym tle
    onSurface = Color(0xFFFFF9C4)
)

private val LightColorScheme = lightColorScheme(
    primary = Yellow40, // Akcent do przycisków, FAB itp.
    secondary = Orange40, // Dodatkowy akcent
    tertiary = Blue40, // Drobne akcenty
    background = Color(0xFFFFFDE7), // Bardzo jasny żółty, jak kartka notatnika
    surface = Color(0xFFFFF9C4), // Żółty do kart tasków
    onPrimary = Color(0xFF232323), // Ciemny tekst na żółtym
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF232323), // Ciemny tekst na jasnym tle
    onSurface = Color(0xFF232323)
)

@Composable
fun ToDoListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

