package io.github.watabee.pdfboxsample

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Spanned
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.EmojiSpan
import kotlin.math.roundToInt

object EmojiProcessor {

    private val Spanned.emojiSpans: Array<EmojiSpan>
        get() = getSpans(0, length, EmojiSpan::class.java)

    fun measureText(text: String, paint: Paint): Float {
        val emojiSpanned = getEmojiSpanned(text)
        val emojiSpans = emojiSpanned?.emojiSpans
        if (emojiSpanned == null || emojiSpans.isNullOrEmpty()) {
            // text が絵文字でなかった場合
            return paint.measureText(text)
        }

        return emojiSpans.sumOf {
            it.getSize(
                paint,
                emojiSpanned,
                emojiSpanned.getSpanStart(it),
                emojiSpanned.getSpanEnd(it),
                paint.fontMetricsInt
            )
        }.toFloat()
    }

    fun drawTextToCanvas(text: String, canvas: Canvas, paint: Paint) {
        val emojiSpanned = getEmojiSpanned(text)
        val emojiSpans = emojiSpanned?.emojiSpans

        if (emojiSpanned == null || emojiSpans.isNullOrEmpty()) {
            // text が絵文字でなかった場合
            canvas.drawText(text, 0f, -paint.fontMetrics.ascent, paint)
        } else {
            val y = -paint.fontMetrics.ascent.roundToInt()
            var x = 0f
            emojiSpans.forEachIndexed { index, emojiSpan ->
                emojiSpan.draw(
                    canvas,
                    emojiSpanned,
                    emojiSpanned.getSpanStart(emojiSpans[index]),
                    emojiSpanned.getSpanEnd(emojiSpans[index]),
                    x,
                    y + paint.fontMetrics.top.roundToInt(),
                    y,
                    y + paint.fontMetrics.bottom.roundToInt(),
                    paint
                )
                x += measureText(text, paint)
            }
        }
    }

    private fun getEmojiSpanned(text: String): Spanned? {
        val processed = if (EmojiCompat.get().loadState == EmojiCompat.LOAD_STATE_SUCCEEDED) EmojiCompat.get().process(text) else null
        return processed as? Spanned
    }
}
