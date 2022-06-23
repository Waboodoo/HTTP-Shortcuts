package ch.rmy.android.http_shortcuts.utils

import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.Locator

interface SimpleXMLContentHandler : ContentHandler {
    override fun setDocumentLocator(locator: Locator?) {
    }

    override fun startDocument() {
    }

    override fun endDocument() {
    }

    override fun startPrefixMapping(prefix: String?, uri: String?) {
    }

    override fun endPrefixMapping(prefix: String?) {
    }

    override fun startElement(uri: String?, localName: String, qName: String?, attributes: Attributes) {
    }

    override fun endElement(uri: String?, localName: String, qName: String?) {
    }

    override fun characters(characters: CharArray, start: Int, length: Int) {
    }

    override fun ignorableWhitespace(ch: CharArray?, start: Int, length: Int) {
    }

    override fun processingInstruction(target: String?, data: String?) {
    }

    override fun skippedEntity(name: String?) {
    }
}
