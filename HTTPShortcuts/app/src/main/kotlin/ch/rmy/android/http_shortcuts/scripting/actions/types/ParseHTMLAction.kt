package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import javax.inject.Inject

class ParseHTMLAction
@Inject
constructor() : Action<ParseHTMLAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): Any {
        val results = Jsoup.parse(htmlInput).select(query).filterIsInstance<Element>().map { node ->
            processNode(node)
        }
        if (query == ":root" && results.size == 1) {
            return results.first()
        }
        return results
    }

    private fun processNode(node: Node): JSONObject {
        val element = createElement(node.normalName(), node.attributes())
        node.childNodes().forEach { childNode ->
            if (childNode is TextNode) {
                val newText = childNode.text()
                val text = element.optString("text") + newText
                element.put("text", text)
            } else if (childNode is Element) {
                element.getJSONArray("children").put(processNode(childNode))
            }
        }
        return element
    }

    private fun createElement(name: String, attributes: Attributes): JSONObject =
        JSONObject()
            .put("name", name)
            .put("attributes", attributes.parse())
            .put("children", JSONArray())

    private fun Attributes.parse(): JSONObject {
        val result = JSONObject()
        forEach { attribute ->
            val attributeName = attribute.key
            val attributeValue = attribute.value
            result.put(attributeName, attributeValue)
        }
        return result
    }

    data class Params(
        val htmlInput: String,
        val query: String,
    )
}
