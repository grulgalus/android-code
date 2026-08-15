package com.codedroid.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codedroid.app.ui.theme.VSCodeTheme
import com.codedroid.app.viewmodel.MainViewModel

@Composable
fun CodeEditor(viewModel: MainViewModel) {
    val tab = viewModel.getActiveTab()
    
    Box(modifier = Modifier.fillMaxSize().background(VSCodeTheme.bgDark)) {
        if (tab != null) {
            BasicTextField(
                value = tab.content,
                onValueChange = { viewModel.updateActiveContent(it) },
                modifier = Modifier.fillMaxSize().padding(16.dp),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color(0xFFD4D4D4)),
                visualTransformation = CodeSyntaxHighlighter(),
                cursorBrush = SolidColor(Color.White)
            )
        } else {
            Text("Žádný soubor není otevřen", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
        }

        // CURSOR AI INLINE PROMPT (Cmd+K)
        if (viewModel.isInlineAiVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .fillMaxWidth(0.8f)
                    .background(Color(0xFF252526), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF007ACC), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                var aiPrompt by remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, "AI", tint = Color(0xFFA78BFA), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        cursorBrush = SolidColor(Color.White),
                        decorationBox = { innerTextField ->
                            if (aiPrompt.isEmpty()) Text("Instrukce pro vygenerování nebo úpravu kódu...", color = Color.Gray, fontSize = 13.sp)
                            innerTextField()
                        }
                    )
                    Button(
                        onClick = { viewModel.runInlineAi(aiPrompt) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Generovat", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
