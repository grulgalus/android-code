package com.codedroid.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codedroid.app.ui.theme.VSCodeTheme

// Tady definujeme PŘESNÝ STYL, který budou sdílet čísla i text!
private val editorTextStyle = androidx.compose.ui.text.TextStyle(
    color = VSCodeTheme.textNormal,
    fontFamily = FontFamily.Monospace,
    fontSize = 14.sp,
    lineHeight = 22.sp // Toto drží řádky přesně zarovnané
)

@Composable
fun CodeEditor(code: String, onCodeChange: (String) -> Unit) {
    val lineCount = code.lines().size.coerceAtLeast(1)

    Row(modifier = Modifier.fillMaxSize().background(VSCodeTheme.bgDark)) {
        
        // Leví pruh: Čísla řádků
        Column(
            modifier = Modifier.width(40.dp).fillMaxHeight().background(VSCodeTheme.bgSidebar).padding(top = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            for (i in 1..lineCount) {
                Text(
                    text = i.toString(),
                    style = editorTextStyle.copy(color = Color(0xFF858585), textAlign = TextAlign.End),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        // Pravá část: Samotný textový editor se zvýrazněním
        BasicTextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier.fillMaxSize().padding(start = 8.dp, top = 8.dp), // Stejný top padding jako u čísel!
            textStyle = editorTextStyle,
            cursorBrush = SolidColor(Color.White),
            visualTransformation = SyntaxHighlightTransformation()
        )
    }
}

class SyntaxHighlightTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(highlightCode(text.text), OffsetMapping.Identity)
    }
}

fun highlightCode(code: String): AnnotatedString {
    val keywords = listOf("fun", "val", "var", "import", "class", "package", "return", "if", "else", "for", "in")
    return buildAnnotatedString {
        append(code)
        keywords.forEach { keyword ->
            val regex = "\\b$keyword\\b".toRegex()
            regex.findAll(code).forEach { result ->
                addStyle(SpanStyle(color = Color(0xFF569CD6)), result.range.first, result.range.last + 1)
            }
        }
        val stringRegex = "\".*?\"".toRegex()
        stringRegex.findAll(code).forEach { result ->
            addStyle(SpanStyle(color = Color(0xFFCE9178)), result.range.first, result.range.last + 1)
        }
    }
}
