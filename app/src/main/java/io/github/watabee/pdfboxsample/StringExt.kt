package io.github.watabee.pdfboxsample

import com.ibm.icu.text.BreakIterator

/**
 * 見た目上の文字（書記素クラスタ）単位で分割したリストを返す.
 */
fun String.divideByGraphemeCluster(): List<String> {
    val iterator = BreakIterator.getCharacterInstance()
    iterator.setText(this)

    val result = mutableListOf<String>()
    var start = iterator.first()
    var end = iterator.next()
    while (end != BreakIterator.DONE) {
        result.add(substring(start, end))
        start = end
        end = iterator.next()
    }

    return result
}

