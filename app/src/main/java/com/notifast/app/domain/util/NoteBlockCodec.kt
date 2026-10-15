package com.notifast.app.domain.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.notifast.app.data.model.CheckItem
import com.notifast.app.data.model.ListItem
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.TextSpan
import com.notifast.app.data.model.TextStyle
import java.lang.reflect.Type

object NoteBlockCodec {

    private const val TYPE_HEADING = "heading"
    private const val TYPE_PARAGRAPH = "paragraph"
    private const val TYPE_BULLET_LIST = "bullet_list"
    private const val TYPE_CHECKLIST = "checklist"
    private const val TYPE_QUOTE = "quote"
    private const val TYPE_IMAGE = "image"
    private const val TYPE_MATH = "math"
    private const val TYPE_DIVIDER = "divider"

    fun registerAdapters(builder: GsonBuilder): GsonBuilder = builder
        .registerTypeAdapter(NoteBlock::class.java, NoteBlockAdapter())
        .registerTypeAdapter(TextStyle::class.java, TextStyleAdapter())

    private val gson: Gson = registerAdapters(GsonBuilder()).create()

    private val listType = object : TypeToken<List<NoteBlock>>() {}.type

    fun toJson(blocks: List<NoteBlock>): String = gson.toJson(blocks, listType)

    fun fromJson(json: String): List<NoteBlock> {
        if (json.isBlank()) return emptyList()
        return try {
            gson.fromJson(json, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun extractPlainText(blocks: List<NoteBlock>): String {
        return blocks.joinToString(separator = "\n") { block ->
            when (block) {
                is NoteBlock.Heading -> block.text
                is NoteBlock.Paragraph -> block.text
                is NoteBlock.Quote -> block.text
                is NoteBlock.BulletList -> block.items.joinToString("\n") { it.text }
                is NoteBlock.Checklist -> block.items.joinToString("\n") { it.text }
                is NoteBlock.Image -> block.caption
                is NoteBlock.MathBlock -> block.source
                is NoteBlock.Divider -> ""
            }
        }.trim()
    }

    fun blocksToMarkdown(blocks: List<NoteBlock>): String =
        blocks.joinToString("\n\n") { block ->
            when (block) {
                is NoteBlock.Heading -> {
                    val prefix = "#".repeat(block.level.coerceIn(1, 3))
                    "$prefix ${spansToMarkdown(block.text, block.spans)}"
                }
                is NoteBlock.Paragraph -> spansToMarkdown(block.text, block.spans)
                is NoteBlock.Quote -> block.text.lines().joinToString("\n") { "> $it" }
                is NoteBlock.BulletList -> block.items.mapIndexed { i, item ->
                    val prefix = if (block.isOrdered) "${i + 1}." else "-"
                    "$prefix ${spansToMarkdown(item.text, item.spans)}"
                }.joinToString("\n")
                is NoteBlock.Checklist -> block.items.joinToString("\n") { item ->
                    val mark = if (item.isChecked) "x" else " "
                    "- [$mark] ${spansToMarkdown(item.text, item.spans)}"
                }
                is NoteBlock.Image -> if (block.caption.isNotBlank()) "![${block.caption}](${block.path})" else "![image](${block.path})"
                is NoteBlock.MathBlock -> "\$\$\n${block.source}\n\$\$"
                is NoteBlock.Divider -> "---"
            }
        }

    fun spansToMarkdown(text: String, spans: List<TextSpan>): String {
        if (spans.isEmpty()) return text
        val n = text.length
        if (n == 0) return text
        val bold = BooleanArray(n)
        val italic = BooleanArray(n)
        val strike = BooleanArray(n)
        spans.forEach { span ->
            val s = span.start.coerceIn(0, n)
            val e = span.end.coerceIn(0, n)
            for (i in s until e) {
                if (TextStyle.BOLD in span.styles) bold[i] = true
                if (TextStyle.ITALIC in span.styles) italic[i] = true
                if (TextStyle.STRIKETHROUGH in span.styles) strike[i] = true
            }
        }
        val sb = StringBuilder()
        var i = 0
        while (i < n) {
            if (bold[i]) {
                val end = runLength(bold, i)
                sb.append("**").append(text, i, end).append("**")
                i = end
            } else if (italic[i]) {
                val end = runLength(italic, i)
                sb.append("*").append(text, i, end).append("*")
                i = end
            } else if (strike[i]) {
                val end = runLength(strike, i)
                sb.append("~~").append(text, i, end).append("~~")
                i = end
            } else {
                sb.append(text[i])
                i++
            }
        }
        return sb.toString()
    }

    private fun runLength(flags: BooleanArray, start: Int): Int {
        var i = start
        while (i < flags.size && flags[i]) i++
        return i
    }

    private class TextStyleAdapter : JsonSerializer<TextStyle>, JsonDeserializer<TextStyle> {
        override fun serialize(src: TextStyle?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive(src?.name ?: "")
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): TextStyle {
            val name = json?.asString ?: return TextStyle.BOLD
            return TextStyle.entries.firstOrNull { it.name == name } ?: TextStyle.BOLD
        }
    }

    private class NoteBlockAdapter : JsonSerializer<NoteBlock>, JsonDeserializer<NoteBlock> {
        override fun serialize(src: NoteBlock?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            val obj = JsonObject()
            when (src) {
                is NoteBlock.Heading -> {
                    obj.addProperty("type", TYPE_HEADING)
                    obj.addProperty("id", src.id)
                    obj.addProperty("text", src.text)
                    obj.addProperty("level", src.level)
                    obj.add("spans", context!!.serialize(src.spans))
                }
                is NoteBlock.Paragraph -> {
                    obj.addProperty("type", TYPE_PARAGRAPH)
                    obj.addProperty("id", src.id)
                    obj.addProperty("text", src.text)
                    obj.add("spans", context!!.serialize(src.spans))
                }
                is NoteBlock.BulletList -> {
                    obj.addProperty("type", TYPE_BULLET_LIST)
                    obj.addProperty("id", src.id)
                    obj.addProperty("isOrdered", src.isOrdered)
                    obj.add("items", context!!.serialize(src.items))
                }
                is NoteBlock.Checklist -> {
                    obj.addProperty("type", TYPE_CHECKLIST)
                    obj.addProperty("id", src.id)
                    obj.add("items", context!!.serialize(src.items))
                }
                is NoteBlock.Quote -> {
                    obj.addProperty("type", TYPE_QUOTE)
                    obj.addProperty("id", src.id)
                    obj.addProperty("text", src.text)
                    obj.add("spans", context!!.serialize(src.spans))
                }
                is NoteBlock.Image -> {
                    obj.addProperty("type", TYPE_IMAGE)
                    obj.addProperty("id", src.id)
                    obj.addProperty("path", src.path)
                    obj.addProperty("caption", src.caption)
                    obj.addProperty("widthRatio", src.widthRatio)
                    src.annotationJson?.let { obj.addProperty("annotationJson", it) }
                }
                is NoteBlock.MathBlock -> {
                    obj.addProperty("type", TYPE_MATH)
                    obj.addProperty("id", src.id)
                    obj.addProperty("source", src.source)
                    obj.addProperty("display", src.display)
                }
                is NoteBlock.Divider -> {
                    obj.addProperty("type", TYPE_DIVIDER)
                    obj.addProperty("id", src.id)
                }
                null -> {}
            }
            return obj
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): NoteBlock {
            val obj = json?.asJsonObject ?: throw IllegalArgumentException("Bloque nulo")
            val type = obj.get("type")?.asString ?: TYPE_PARAGRAPH
            val id = obj.get("id")?.takeIf { it.asString.isNotBlank() }?.asString
                ?: java.util.UUID.randomUUID().toString()
            return when (type) {
                TYPE_HEADING -> NoteBlock.Heading(
                    id = id,
                    text = obj.get("text")?.asString ?: "",
                    level = obj.get("level")?.asInt ?: 1,
                    spans = parseSpans(obj, context)
                )
                TYPE_PARAGRAPH -> NoteBlock.Paragraph(
                    id = id,
                    text = obj.get("text")?.asString ?: "",
                    spans = parseSpans(obj, context)
                )
                TYPE_BULLET_LIST -> NoteBlock.BulletList(
                    id = id,
                    items = parseListItems(obj, context),
                    isOrdered = obj.get("isOrdered")?.asBoolean ?: false
                )
                TYPE_CHECKLIST -> NoteBlock.Checklist(
                    id = id,
                    items = parseCheckItems(obj, context)
                )
                TYPE_QUOTE -> NoteBlock.Quote(
                    id = id,
                    text = obj.get("text")?.asString ?: "",
                    spans = parseSpans(obj, context)
                )
                TYPE_IMAGE -> NoteBlock.Image(
                    id = id,
                    path = obj.get("path")?.asString ?: "",
                    caption = obj.get("caption")?.asString ?: "",
                    widthRatio = obj.get("widthRatio")?.asFloat ?: 1f,
                    annotationJson = obj.get("annotationJson")?.asString
                )
                TYPE_MATH -> NoteBlock.MathBlock(
                    id = id,
                    source = obj.get("source")?.asString ?: "",
                    display = obj.get("display")?.asBoolean ?: true
                )
                TYPE_DIVIDER -> NoteBlock.Divider(id = id)
                else -> NoteBlock.Paragraph(id = id, text = obj.get("text")?.asString ?: "")
            }
        }

        private fun parseSpans(obj: JsonObject, context: JsonDeserializationContext?): List<TextSpan> {
            val spansElem = obj.get("spans") ?: return emptyList()
            val spanListType = object : TypeToken<List<TextSpan>>() {}.type
            return context!!.deserialize(spansElem, spanListType)
        }

        private fun parseListItems(obj: JsonObject, context: JsonDeserializationContext?): List<ListItem> {
            val itemsElem = obj.get("items") ?: return emptyList()
            val itemListType = object : TypeToken<List<ListItem>>() {}.type
            return context!!.deserialize(itemsElem, itemListType)
        }

        private fun parseCheckItems(obj: JsonObject, context: JsonDeserializationContext?): List<CheckItem> {
            val itemsElem = obj.get("items") ?: return emptyList()
            val checkListType = object : TypeToken<List<CheckItem>>() {}.type
            return context!!.deserialize(itemsElem, checkListType)
        }
    }
}
