package com.danignat.ark.ui.pages.command

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danignat.ark.ui.navigation.NavigationCallbacks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandLineScreen(
    modifier: Modifier,
    navigation: NavigationCallbacks
) {
    var command by remember { mutableStateOf("") }
    val commandHistory = remember { mutableStateListOf<CommandEntry>() }
    val scrollState = rememberScrollState()

    // Use your app's global theme (dark mode assumed)
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val border = MaterialTheme.colorScheme.outlineVariant
    val text = MaterialTheme.colorScheme.onSurface
    val prompt = MaterialTheme.colorScheme.tertiary

    val executeCommand: () -> Unit = {
        if (command.isNotBlank()) {
            commandHistory.add(
                CommandEntry(
                    command = command,
                    output = "Output (mock): You entered: \"$command\""
                )
            )
            command = ""
        }
    }

    Scaffold(
        containerColor = background
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Terminal output area - full screen terminal look
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(surface)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Welcome banner
                    if (commandHistory.isEmpty()) {
                        Text(
                            text = "╔═══════════════════════════════════════╗",
                            color = text.copy(alpha = 0.4f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "║       Welcome to ARK Terminal v1.0       ║",
                            color = text.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "╚═══════════════════════════════════════╝",
                            color = text.copy(alpha = 0.4f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Type a command and press Enter or Send.",
                            color = text.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }

                    // Command history
                    commandHistory.forEach { entry ->
                        Spacer(Modifier.height(12.dp))

                        // Command line
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "❯",
                                color = prompt,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = entry.command,
                                color = text,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Output
                        if (entry.output.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = entry.output,
                                color = text.copy(alpha = 0.85f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(start = 22.dp)
                            )
                        }
                    }
                }
            }

            // Terminal divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(border)
            )

            // Input row - terminal prompt style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prompt character
                Text(
                    text = "❯",
                    color = prompt,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Command input
                BasicTextField(
                    value = command,
                    onValueChange = { command = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { executeCommand() }
                    ),
                    cursorBrush = SolidColor(prompt),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (command.isEmpty()) {
                            Text(
                                text = "echo \"Hello ark\"",
                                color = text.copy(alpha = 0.35f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )

                IconButton(onClick = executeCommand) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = prompt
                    )
                }
            }
        }
    }
}

data class CommandEntry(
    val command: String,
    val output: String
)

