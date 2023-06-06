package helper

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object XmlHelper {
    data class TagNode(
        val name: String,
        val children: List<TagNode> = emptyList(),
        val outputPrefix: String = ""
    )

    fun processTag(
        parser: XmlPullParser,
        tagHierarchy: TagNode,
        init: (String) -> Unit = {},
    ) {
        val tagStack = mutableListOf<TagNode>()
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val currentTag = tagStack.lastOrNull()

                    if (currentTag != null && parser.name in currentTag.children.map { it.name }) {
                        val newTag = currentTag.children.first { it.name == parser.name }
                        tagStack.add(newTag)
                    } else if (tagStack.isEmpty() && parser.name == tagHierarchy.name) {
                        tagStack.add(tagHierarchy)
                    }
                }
                XmlPullParser.TEXT -> {
                    val currentTag = tagStack.lastOrNull()
                    if (currentTag != null && currentTag.children.isEmpty()) {
                        val text = parser.text.trim()
                        if (text.isNotEmpty()) {
                            init("${currentTag.outputPrefix}$text")
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagStack.isNotEmpty() && parser.name == tagStack.last().name) {
                        tagStack.removeAt(tagStack.size - 1)
                    }
                }
            }
            eventType = parser.next()
        }
    }

    fun findTagInString(
        xml: String,
        tagHierarchy: TagNode,
        init: (String) -> Unit = {},
    ) {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))
        processTag(parser, tagHierarchy, init)
    }
}