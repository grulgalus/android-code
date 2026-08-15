package com.codedroid.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codedroid.app.ui.theme.VSCodeTheme

@Composable
fun CodeEditor(code: String, onCodeChange: (String) -> Unit) {
    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        modifier = Modifier
            .fillMaxSize()
            .background(VSCodeTheme.bgDark)
            .padding(16.dp),
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = Color(0xFFD4D4D4) // Výchozí barva textu (světle šedá)
        ),
        visualTransformation = CodeSyntaxHighlighter(), // ZDE JE TO KOUZLO!
        cursorBrush = SolidColor(Color.White)
    )
}
