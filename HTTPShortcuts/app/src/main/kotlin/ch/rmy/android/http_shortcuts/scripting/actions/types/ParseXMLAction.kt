package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.util.Xml
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.SimpleXMLContentHandler
import ch.rmy.android.scripting.JsObject
import ch.rmy.android.scripting.ScriptingEngine
import java.util.Stack
import javax.inject.Inject
import org.xml.sax.Attributes

class ParseXMLAction
@Inject
constructor() : Action<ParseXMLAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): JsObject {
        var root: Node? = null
        var activeElement: Node? = null
        val elementStack = Stack<Node>()
        try {
            Xml.parse(
                xmlInput,
                object : SimpleXMLContentHandler {
                    override fun startElement(uri: String?, localName: String, qName: String?, attributes: Attributes) {
                        val element = Node(localName, attributes.parse())
                        activeElement
                            ?.children
                            ?.add(element)
                            ?: run {
                                root = element
                            }
                        elementStack.push(element)
                        activeElement = element
                    }

                    override fun endElement(uri: String?, localName: String, qName: String?) {
                        elementStack.pop()
                        activeElement = elementStack.lastOrNull()
                    }

                    override fun characters(characters: CharArray, start: Int, length: Int) {
                        val element = activeElement ?: return
                        element.text = element.text.orEmpty() + characters.slice(start until (start + length)).joinToString(separator = "")
                    }
                },
            )
        } catch (e: Throwable) {
            if (!e.javaClass.name.contains("ParseException")) {
                logException(e)
            }
            throw ActionException {
                getString(R.string.error_invalid_xml, e.message)
            }
        }
        return root!!.toJsObject(executionContext.scriptingEngine)
    }

    private fun Attributes.parse(): Map<String, String> =
        buildMap {
            for (i in 0 until length) {
                val attributeName = getLocalName(i)
                val attributeValue = getValue(i)
                put(attributeName, attributeValue)
            }
        }

    private fun Node.toJsObject(scriptingEngine: ScriptingEngine): JsObject =
        scriptingEngine.buildJsObject {
            property("name", name)
            property(
                "attributes",
                scriptingEngine.buildJsObject {
                    attributes.forEach { (key, value) ->
                        property(key, value)
                    }
                },
            )
            if (text != null) {
                property("text", text)
            }
            objectListProperty("children", children.map { it.toJsObject(scriptingEngine) })
        }

    private data class Node(
        val name: String,
        val attributes: Map<String, String>,
        var text: String? = null,
        val children: MutableList<Node> = mutableListOf(),
    )

    data class Params(
        val xmlInput: String,
    )
}
