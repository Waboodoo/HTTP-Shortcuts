package ch.rmy.android.http_shortcuts.shell_apk

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import javax.inject.Inject

/**
 * Performs narrow string replacement in an Android binary XML document.
 *
 * The shell APK template is already compiled when it is bundled into the main app, so its manifest is not plain XML
 * anymore. For the generated shell APKs we only need to replace placeholder strings in the manifest string pool
 * (package name, app label, and target URI), leaving the XML tree structure untouched.
 */
class BinaryXmlStringPoolEditor
@Inject
constructor() {

    fun replaceStrings(binaryXml: ByteArray, replacements: Map<String, String>): ByteArray {
        if (binaryXml.readUShort(0) != RES_XML_TYPE) {
            error("Not an Android binary XML document")
        }

        val stringPoolOffset = XML_HEADER_SIZE
        if (binaryXml.readUShort(stringPoolOffset) != RES_STRING_POOL_TYPE) {
            error("Binary XML string pool not found")
        }

        val oldStringPoolSize = binaryXml.readIntLE(stringPoolOffset + OFFSET_CHUNK_SIZE)
        val newStringPool = rebuildStringPool(
            chunk = binaryXml.copyOfRange(stringPoolOffset, stringPoolOffset + oldStringPoolSize),
            replacements = replacements,
        )
        val newXmlSize = binaryXml.readIntLE(OFFSET_CHUNK_SIZE) + newStringPool.size - oldStringPoolSize

        return ByteArrayOutputStream(newXmlSize).use { output ->
            output.write(binaryXml, 0, stringPoolOffset)
            output.write(newStringPool)
            output.write(binaryXml, stringPoolOffset + oldStringPoolSize, binaryXml.size - stringPoolOffset - oldStringPoolSize)
            output.toByteArray().also {
                it.writeIntLE(OFFSET_CHUNK_SIZE, newXmlSize)
            }
        }
    }

    private fun rebuildStringPool(chunk: ByteArray, replacements: Map<String, String>): ByteArray {
        val headerSize = chunk.readUShort(OFFSET_HEADER_SIZE)
        val stringCount = chunk.readIntLE(OFFSET_STRING_COUNT)
        val styleCount = chunk.readIntLE(OFFSET_STYLE_COUNT)
        val flags = chunk.readIntLE(OFFSET_FLAGS)
        val stringsStart = chunk.readIntLE(OFFSET_STRINGS_START)
        val stylesStart = chunk.readIntLE(OFFSET_STYLES_START)
        val utf8 = flags and UTF8_FLAG != 0
        val styleData = if (stylesStart == 0) {
            ByteArray(0)
        } else {
            chunk.copyOfRange(stylesStart, chunk.size)
        }

        val strings = (0 until stringCount).map { index ->
            val stringOffset = chunk.readIntLE(headerSize + index * 4)
            val value = if (utf8) {
                chunk.readUtf8String(stringsStart + stringOffset)
            } else {
                chunk.readUtf16String(stringsStart + stringOffset)
            }
            replacements[value] ?: value
        }

        val stringDataOutput = ByteArrayOutputStream()
        val stringOffsets = IntArray(stringCount)
        strings.forEachIndexed { index, value ->
            stringOffsets[index] = stringDataOutput.size()
            stringDataOutput.write(if (utf8) value.encodeUtf8String() else value.encodeUtf16String())
        }
        while (stringDataOutput.size() % 4 != 0) {
            stringDataOutput.write(0)
        }

        val newStringData = stringDataOutput.toByteArray()
        val newStylesStart = if (styleCount == 0) 0 else stringsStart + newStringData.size
        val newChunkSize = stringsStart + newStringData.size + styleData.size
        val output = chunk.copyOfRange(0, stringsStart).copyOf(newChunkSize)

        // Replacing labels or package names can change the byte length of the pool, so all string offsets and chunk
        // sizes are rebuilt rather than patched in-place.
        output.writeIntLE(OFFSET_CHUNK_SIZE, newChunkSize)
        output.writeIntLE(OFFSET_STYLES_START, newStylesStart)
        stringOffsets.forEachIndexed { index, offset ->
            output.writeIntLE(headerSize + index * 4, offset)
        }
        newStringData.copyInto(output, stringsStart)
        if (styleData.isNotEmpty()) {
            styleData.copyInto(output, newStylesStart)
        }
        return output
    }

    private fun ByteArray.readUtf8String(offset: Int): String {
        var cursor = offset
        cursor += readLength8(cursor).byteCount
        val byteLength = readLength8(cursor)
        cursor += byteLength.byteCount
        return String(this, cursor, byteLength.value, Charsets.UTF_8)
    }

    private fun ByteArray.readUtf16String(offset: Int): String {
        val length = readLength16(offset)
        val cursor = offset + length.byteCount
        return String(this, cursor, length.value * 2, UTF_16LE)
    }

    private fun String.encodeUtf8String(): ByteArray {
        val bytes = toByteArray(Charsets.UTF_8)
        return ByteArrayOutputStream().use { output ->
            output.writeLength8(length)
            output.writeLength8(bytes.size)
            output.write(bytes)
            output.write(0)
            output.toByteArray()
        }
    }

    private fun String.encodeUtf16String(): ByteArray {
        val bytes = toByteArray(UTF_16LE)
        return ByteArrayOutputStream().use { output ->
            output.writeLength16(length)
            output.write(bytes)
            output.write(0)
            output.write(0)
            output.toByteArray()
        }
    }

    private fun ByteArray.readLength8(offset: Int): EncodedLength {
        val first = this[offset].toInt() and 0xFF
        return if (first and 0x80 == 0) {
            EncodedLength(first, 1)
        } else {
            EncodedLength(((first and 0x7F) shl 8) or (this[offset + 1].toInt() and 0xFF), 2)
        }
    }

    private fun ByteArray.readLength16(offset: Int): EncodedLength {
        val first = readUShort(offset)
        return if (first and 0x8000 == 0) {
            EncodedLength(first, 2)
        } else {
            EncodedLength(((first and 0x7FFF) shl 16) or readUShort(offset + 2), 4)
        }
    }

    private fun ByteArrayOutputStream.writeLength8(length: Int) {
        if (length > 0x7F) {
            write((length shr 8) or 0x80)
        }
        write(length and 0xFF)
    }

    private fun ByteArrayOutputStream.writeLength16(length: Int) {
        if (length > 0x7FFF) {
            write(((length shr 16) and 0x7F) or 0x80)
            write((length shr 24) and 0xFF)
        }
        write(length and 0xFF)
        write((length shr 8) and 0xFF)
    }

    private fun ByteArray.readUShort(offset: Int): Int =
        (this[offset].toInt() and 0xFF) or ((this[offset + 1].toInt() and 0xFF) shl 8)

    private fun ByteArray.readIntLE(offset: Int): Int =
        (this[offset].toInt() and 0xFF) or
            ((this[offset + 1].toInt() and 0xFF) shl 8) or
            ((this[offset + 2].toInt() and 0xFF) shl 16) or
            ((this[offset + 3].toInt() and 0xFF) shl 24)

    private fun ByteArray.writeIntLE(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value shr 8) and 0xFF).toByte()
        this[offset + 2] = ((value shr 16) and 0xFF).toByte()
        this[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private data class EncodedLength(
        val value: Int,
        val byteCount: Int,
    )

    companion object {
        private const val RES_STRING_POOL_TYPE = 0x0001
        private const val RES_XML_TYPE = 0x0003
        private const val XML_HEADER_SIZE = 8
        private const val UTF8_FLAG = 0x00000100

        private const val OFFSET_HEADER_SIZE = 2
        private const val OFFSET_CHUNK_SIZE = 4
        private const val OFFSET_STRING_COUNT = 8
        private const val OFFSET_STYLE_COUNT = 12
        private const val OFFSET_FLAGS = 16
        private const val OFFSET_STRINGS_START = 20
        private const val OFFSET_STYLES_START = 24

        private val UTF_16LE = Charset.forName("UTF-16LE")
    }
}
