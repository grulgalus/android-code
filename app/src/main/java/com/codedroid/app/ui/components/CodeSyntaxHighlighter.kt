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
            
            val keywordStyle = SpanStyle(color = Color(0xFFC586C0), fontWeight = FontWeight.Bold) 
            val stringStyle = SpanStyle(color = Color(0xFFCE9178)) 
            val commentStyle = SpanStyle(color = Color(0xFF6A9955)) 
            val functionStyle = SpanStyle(color = Color(0xFFDCDCAA)) 
            val numberStyle = SpanStyle(color = Color(0xFFB5CEA8)) 
            val typeStyle = SpanStyle(color = Color(0xFF4EC9B0)) 

            // Klíčová slova (JS, TS, Kotlin, Python...)
            val keywords = Pattern.compile("\\b(const|let|package|import|class|fun|val|var|if|else|for|while|return|when|try|catch|finally|interface|object|typealias|function|public|private)\\b")
            val functions = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*(?=\\s*\$)")
            val types = Pattern.compile("(?<=:\\s)[A-Z][a-zA-Z0-9_]*|\\b[A-Z][a-zA-Z0-9_]*(?=\\s*\\{)")
            val numbers = Pattern.compile("\\b\\d+\\b")
            val strings = Pattern.compile("\".*?\"|'.*?'|`.*?`")
            
            // OPRAVA KOMENTÁŘŮ: 
            // 1. Jednořádkové komentáře se ZASTAVÍ na konci řádku (bez Pattern.DOTALL)
            val singleLineComments = Pattern.compile("//.*")
            // 2. Víceřádkové komentáře: [\s\S] znamená absolutně cokoliv včetně nových řádků
            val multiLineComments = Pattern.compile("/\\*[\\s\\S]*?\\*/")

            // Pořadí je extrémně důležité! Komentáře a stringy přepisují vše ostatní.
            applyStyle(code, keywords, keywordStyle)
            applyStyle(code, functions, functionStyle)
            applyStyle(code, types, typeStyle)
            applyStyle(code, numbers, numberStyle)
            
            applyStyle(code, strings, stringStyle)
            
            applyStyle(code, singleLineComments, commentStyle)
            applyStyle(code, multiLineComments, commentStyle)
        }
    }

    private fun AnnotatedString.Builder.applyStyle(text: String, pattern: Pattern, style: SpanStyle) {
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            addStyle(style, matcher.start(), matcher.end())
        }
    }
}
