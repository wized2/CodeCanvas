package com.endroid.code.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * Lightweight regex-based syntax highlighter for the editor.
 * Produces AnnotatedString with colored spans for keywords, strings, comments, etc.
 */
object SyntaxHighlighter {

    data class ThemeColors(
        val keyword: Color,
        val string: Color,
        val comment: Color,
        val number: Color,
        val function: Color,
        val type: Color,
        val operator: Color,
        val punctuation: Color,
        val default: Color
    )

    val darkColors = ThemeColors(
        keyword = Color(0xFFBB9AF7),
        string = Color(0xFF9ECE6A),
        comment = Color(0xFF565F89),
        number = Color(0xFFFF9E64),
        function = Color(0xFF7AA2F7),
        type = Color(0xFF2AC3DE),
        operator = Color(0xFF89DDFF),
        punctuation = Color(0xFFA9B1D6),
        default = Color(0xFFC0CAF5)
    )

    val lightColors = ThemeColors(
        keyword = Color(0xFF5C2D91),
        string = Color(0xFF0A7A0A),
        comment = Color(0xFF6A737D),
        number = Color(0xFFB35C00),
        function = Color(0xFF0550AE),
        type = Color(0xFF0550AE),
        operator = Color(0xFFCF222E),
        punctuation = Color(0xFF24292F),
        default = Color(0xFF24292F)
    )

    private val kotlinKeywords = setOf(
        "package", "import", "class", "interface", "object", "fun", "val", "var",
        "if", "else", "when", "for", "while", "do", "return", "break", "continue",
        "try", "catch", "finally", "throw", "in", "is", "as", "by", "this", "super",
        "true", "false", "null", "typealias", "data", "sealed", "enum", "annotation",
        "companion", "init", "constructor", "override", "open", "abstract", "final",
        "private", "protected", "public", "internal", "suspend", "inline", "reified",
        "lateinit", "const", "operator", "infix", "tailrec", "external", "actual",
        "expect", "where", "get", "set", "field", "it", "with", "apply", "let", "also", "run"
    )

    private val javaKeywords = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new", "package",
        "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient",
        "try", "void", "volatile", "while", "true", "false", "null", "var", "record",
        "sealed", "permits", "non-sealed", "yield"
    )

    private val pythonKeywords = setOf(
        "False", "None", "True", "and", "as", "assert", "async", "await", "break",
        "class", "continue", "def", "del", "elif", "else", "except", "finally",
        "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal",
        "not", "or", "pass", "raise", "return", "try", "while", "with", "yield"
    )

    private val jsKeywords = setOf(
        "break", "case", "catch", "class", "const", "continue", "debugger", "default",
        "delete", "do", "else", "export", "extends", "finally", "for", "function",
        "if", "import", "in", "instanceof", "new", "return", "super", "switch",
        "this", "throw", "try", "typeof", "var", "void", "while", "with", "yield",
        "let", "static", "enum", "await", "async", "true", "false", "null", "undefined",
        "of", "from", "as", "get", "set"
    )

    private val sqlKeywords = setOf(
        "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP",
        "ALTER", "TABLE", "INDEX", "VIEW", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER",
        "ON", "AS", "AND", "OR", "NOT", "IN", "BETWEEN", "LIKE", "IS", "NULL",
        "ORDER", "BY", "GROUP", "HAVING", "LIMIT", "OFFSET", "UNION", "ALL", "DISTINCT",
        "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "CONSTRAINT", "DEFAULT", "VALUES",
        "INTO", "SET", "TRUE", "FALSE"
    )

    fun highlight(
        text: String,
        language: Language,
        isDark: Boolean
    ): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")
        val colors = if (isDark) darkColors else lightColors
        val keywords = when (language) {
            Language.KOTLIN -> kotlinKeywords
            Language.JAVA, Language.C -> javaKeywords
            Language.PYTHON -> pythonKeywords
            Language.JAVASCRIPT, Language.TYPESCRIPT -> jsKeywords
            Language.SQL -> sqlKeywords
            else -> emptySet()
        }

        return buildAnnotatedString {
            // Simple tokenizer approach: process line by line for comments, then tokens
            var i = 0
            val len = text.length

            while (i < len) {
                // Multi-line / single-line comments
                when {
                    language == Language.XML || language == Language.XML || language == Language.MARKDOWN -> {
                        // Basic tag highlighting for XML-like
                        if (text.startsWith("<!--", i)) {
                            val end = text.indexOf("-->", i).let { if (it == -1) len else it + 3 }
                            withStyle(SpanStyle(color = colors.comment)) {
                                append(text.substring(i, end))
                            }
                            i = end
                            continue
                        }
                        if (text[i] == '<' ) {
                            val end = text.indexOf('>', i).let { if (it == -1) len else it + 1 }
                            withStyle(SpanStyle(color = colors.keyword, fontWeight = FontWeight.Medium)) {
                                append(text.substring(i, end))
                            }
                            i = end
                            continue
                        }
                    }
                    // C-style comments
                    text.startsWith("//", i) && language !in listOf(Language.PYTHON, Language.SQL) -> {
                        val end = text.indexOf('\n', i).let { if (it == -1) len else it }
                        withStyle(SpanStyle(color = colors.comment)) {
                            append(text.substring(i, end))
                        }
                        i = end
                        continue
                    }
                    text.startsWith("/*", i) && language !in listOf(Language.PYTHON, Language.SQL) -> {
                        val end = text.indexOf("*/", i).let { if (it == -1) len else it + 2 }
                        withStyle(SpanStyle(color = colors.comment)) {
                            append(text.substring(i, end))
                        }
                        i = end
                        continue
                    }
                    // Python comments
                    text[i] == '#' && language == Language.PYTHON -> {
                        val end = text.indexOf('\n', i).let { if (it == -1) len else it }
                        withStyle(SpanStyle(color = colors.comment)) {
                            append(text.substring(i, end))
                        }
                        i = end
                        continue
                    }
                    // SQL comments
                    text.startsWith("--", i) && language == Language.SQL -> {
                        val end = text.indexOf('\n', i).let { if (it == -1) len else it }
                        withStyle(SpanStyle(color = colors.comment)) {
                            append(text.substring(i, end))
                        }
                        i = end
                        continue
                    }
                    // Strings
                    text[i] == '"' || text[i] == '\'' -> {
                        val quote = text[i]
                        var j = i + 1
                        while (j < len) {
                            if (text[j] == '\\' && j + 1 < len) {
                                j += 2
                                continue
                            }
                            if (text[j] == quote) {
                                j++
                                break
                            }
                            j++
                        }
                        withStyle(SpanStyle(color = colors.string)) {
                            append(text.substring(i, j))
                        }
                        i = j
                        continue
                    }
                    // Numbers
                    text[i].isDigit() -> {
                        var j = i
                        while (j < len && (text[j].isDigit() || text[j] == '.' || text[j] == 'x' || text[j] in 'a'..'f' || text[j] in 'A'..'F')) {
                            j++
                        }
                        withStyle(SpanStyle(color = colors.number)) {
                            append(text.substring(i, j))
                        }
                        i = j
                        continue
                    }
                    // Identifiers / keywords
                    text[i].isLetter() || text[i] == '_' -> {
                        var j = i
                        while (j < len && (text[j].isLetterOrDigit() || text[j] == '_')) {
                            j++
                        }
                        val word = text.substring(i, j)
                        val isKeyword = keywords.contains(word) ||
                                (language == Language.SQL && keywords.contains(word.uppercase()))
                        // Check if next is ( for function-ish
                        var k = j
                        while (k < len && text[k].isWhitespace()) k++
                        val isFunc = k < len && text[k] == '('

                        when {
                            isKeyword -> withStyle(SpanStyle(color = colors.keyword, fontWeight = FontWeight.SemiBold)) {
                                append(word)
                            }
                            isFunc -> withStyle(SpanStyle(color = colors.function)) {
                                append(word)
                            }
                            word.first().isUpperCase() && language in listOf(Language.KOTLIN, Language.JAVA, Language.TYPESCRIPT) ->
                                withStyle(SpanStyle(color = colors.type)) {
                                    append(word)
                                }
                            else -> withStyle(SpanStyle(color = colors.default)) {
                                append(word)
                            }
                        }
                        i = j
                        continue
                    }
                    // Operators / punctuation
                    text[i] in "+-*/%=<>!&|^~?:;,.()[]{}" -> {
                        withStyle(SpanStyle(color = colors.operator)) {
                            append(text[i])
                        }
                        i++
                        continue
                    }
                    else -> {
                        withStyle(SpanStyle(color = colors.default)) {
                            append(text[i])
                        }
                        i++
                    }
                }
            }
        }
    }
}
