package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.util.Xml
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.SimpleXMLContentHandler
import io.reactivex.Single
import org.json.JSONArray
import org.json.JSONObject
import org.xml.sax.Attributes
import java.util.Stack

class ParseXMLAction(private val xmlInput: String) : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<Any> =
        Single.create { emitter ->
            var root: JSONObject? = null
            var activeElement: JSONObject? = null
            val elementStack = Stack<JSONObject>()
            try {
                Xml.parse(
                    xmlInput,
                    object : SimpleXMLContentHandler {

                        override fun startElement(uri: String?, localName: String, qName: String?, attributes: Attributes) {
                            val element = createElement(localName, attributes)
                            activeElement
                                ?.getJSONArray("children")
                                ?.put(element)
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
                            val text = element.optString("text") + characters.slice(start until (start + length)).joinToString(separator = "")
                            element.put("text", text)
                        }
                    }
                )
            } catch (e: Throwable) {
                if (!e.javaClass.name.contains("ParseException")) {
                    logException(e)
                }
                throw ActionException {
                    // TODO: Localize error message
                    "Invalid XML: ${e.message}"
                }
            }
            emitter.onSuccess(root!!)
        }

    private fun createElement(name: String, attributes: Attributes): JSONObject =
        JSONObject()
            .put("name", name)
            .put("attributes", attributes.parse())
            .put("children", JSONArray())

    private fun Attributes.parse(): JSONObject {
        val result = JSONObject()
        for (i in 0 until length) {
            val attributeName = getLocalName(i)
            val attributeValue = getValue(i)
            result.put(attributeName, attributeValue)
        }
        return result
    }
}
