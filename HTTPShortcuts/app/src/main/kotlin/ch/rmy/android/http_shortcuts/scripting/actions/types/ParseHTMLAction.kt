package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.scripting.JsObject
import ch.rmy.android.scripting.ScriptingEngine
import javax.inject.Inject
import org.jsoup.Jsoup
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.DataNode
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.Selector.SelectorParseException

class ParseHTMLAction
@Inject
constructor() : Action<ParseHTMLAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): Any {
        val results = try {
            Jsoup.parse(htmlInput).select(query).filterIsInstance<Element>().map { node ->
                processNode(executionContext.scriptingEngine, node)
            }
        } catch (e: SelectorParseException) {
            throw ActionException {
                "Error in parseHTML: ${e.message}"
            }
        }
        if (query == ":root" && results.size == 1) {
            return results.first()
        }
        return results
    }

    private fun processNode(scriptingEngine: ScriptingEngine, node: Node): JsObject =
        scriptingEngine.buildJsObject {
            property("name", node.normalName())
            property("attributes", node.attributes().parse(scriptingEngine))
            val children = mutableListOf<JsObject>()
            var text: String? = null
            node.childNodes().forEach { childNode ->
                when (childNode) {
                    is TextNode -> {
                        val newText = childNode.text()
                        text = text.orEmpty() + newText
                    }
                    is DataNode -> {
                        val newText = childNode.wholeData
                        text = text.orEmpty() + newText
                    }
                    is Element -> {
                        children.add(processNode(scriptingEngine, childNode))
                    }
                }
            }
            if (text != null) {
                property("text", text)
            }
            objectListProperty("children", children)
        }

    private fun Attributes.parse(scriptingEngine: ScriptingEngine): JsObject =
        scriptingEngine.buildJsObject {
            forEach { attribute ->
                property(attribute.key, attribute.value)
            }
        }

    data class Params(
        val htmlInput: String,
        val query: String,
    )
}
