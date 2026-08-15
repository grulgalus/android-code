package com.codedroid.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.regex.Pattern

class CodeSyntaxHighlighter : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            highlightCode(text.text),
            OffsetMapping.Identity
        )
    }

    private fun highlightCode(code: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            
            // Definice barev inspirované VS Code (Dark Theme)
            val keywordStyle = SpanStyle(color = Color(0xFFC586C0), fontWeight = FontWeight.Bold) // Fialová
            val stringStyle = SpanStyle(color = Color(0xFFCE9178)) // Oranžová
            val commentStyle = SpanStyle(color = Color(0xFF6A9955)) // Zelená
            val functionStyle = SpanStyle(color = Color(0xFFDCDCAA)) // Žlutá
            val numberStyle = SpanStyle(color = Color(0xFFB5CEA8)) // Zelenkavá
            val typeStyle = SpanStyle(color = Color(0xFF4EC9B0)) // Modrozelená (Třídy/Typy)

            // Regulární výrazy pro různé elementy kódu
            val keywords = Pattern.compile("\\b(package|import|class|fun|val|var|if|else|for|while|return|when|try|catch|finally|interface|object|typealias|const|public|private|protected|internal|override)\\b")
            val strings = Pattern.compile("\".*?\"|\'.*?\'")
            val comments = Pattern.compile("//.*|/\\*.*?\\*/", Pattern.DOTALL)
            val numbers = Pattern.compile("\\b\\d+\\b")
            val functions = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*(?=\\s*\$)")
            val types = Pattern.compile("(?<=:\\s)[A-Z][a-zA-Z0-9_]*|\\b[A-Z][a-zA-Z0-9_]*(?=\\s*\\{)")

            // Aplikace stylů
            applyStyle(code, keywords, keywordStyle)
            applyStyle(code, functions, functionStyle)
            applyStyle(code, types, typeStyle)
            applyStyle(code, numbers, numberStyle)
            applyStyle(code, strings, stringStyle)
            applyStyle(code, comments, commentStyle) // Komentáře musí být poslední, aby přepsaly vše uvnitř
        }
    }

    private fun AnnotatedString.Builder.applyStyle(text: String, pattern: Pattern, style: SpanStyle) {
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            addStyle(style, matcher.start(), matcher.end())
        }
    }
}
