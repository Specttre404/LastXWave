package com.lastwave.app.ui.search

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
    onOpenAudioRecognition: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Voice to text intent launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                onQueryChange(spokenText)
                onSearch(spokenText)
            }
        }
    }

    // Audio record permission launcher for sound recognition
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onOpenAudioRecognition()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search songs, albums, artists...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 1. Voice Search Button
                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search on LastXWave...")
                            }
                            try {
                                voiceLauncher.launch(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Voice Search",
                            tint = Color(0xFF00E5FF)
                        )
                    }

                    // 2. Sound Recognition Button (Distinct Equalizer Icon)
                    IconButton(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                onOpenAudioRecognition()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Identify Playing Music",
                            tint = Color(0xFFFF2E93)
                        )
                    }
                }
            }
        }
    }
}
