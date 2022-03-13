package ch.rmy.favicongrabber.utils

import ch.rmy.favicongrabber.models.LinkTag

object HTMLUtil {

    private val LINK_TAG_PATTERN = """<link\s+[^>]*rel=["']([a-z -]+)["'][^>]*>""".toRegex(RegexOption.IGNORE_CASE)
    private val REL_PATTERN = """\srel=("[^"]+"|'[^']+'|[\S]+)[\s/>]""".toRegex(RegexOption.IGNORE_CASE)
    private val HREF_PATTERN = """\shref=("[^"]+"|'[^']+'|[\S]+)[\s/>]""".toRegex(RegexOption.IGNORE_CASE)
    private val SIZES_PATTERN = """\ssizes=("[0-9]+x[0-9]+"|'[0-9]+x[0-9]+'|[\S]+)[\s/>]""".toRegex(RegexOption.IGNORE_CASE)

    fun findLinkTags(html: String, relevantRel: Set<String>): Sequence<LinkTag> =
        LINK_TAG_PATTERN.findAll(html)
            .mapNotNull { matchResult ->
                parseLinkTag(matchResult.value)
            }
            .filter { linkTag ->
                linkTag.rel in relevantRel
            }

    private fun parseLinkTag(tag: String): LinkTag? {
        return LinkTag(
            rel = parseAttribute(tag, REL_PATTERN)
                ?.lowercase()
                ?: return null,
            href = parseAttribute(tag, HREF_PATTERN)
                ?: return null,
            size = parseAttribute(tag, SIZES_PATTERN)
                ?.split('x')
                ?.get(0)
                ?.toIntOrNull(),
        )
    }

    private fun parseAttribute(tag: String, regex: Regex): String? =
        regex
            .find(tag)
            ?.groupValues
            ?.get(1)
            ?.trim('\'', '"')
}
