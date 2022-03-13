package ch.rmy.favicongrabber.utils

fun <T> createComparator(preferredSize: Int, getSize: T.() -> Int?): Comparator<T> =
    compareBy { item ->
        val size = item.getSize()
        if (size == null) {
            Int.MAX_VALUE
        } else {
            val difference = size - preferredSize
            if (difference < 0) {
                // Icon is smaller than desired, treat it with low priority
                difference * -100
            } else {
                difference
            }
        }
    }
