package io.github.watabee.pdfboxsample

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import androidx.annotation.ColorInt
import com.tom_roush.harmony.awt.geom.AffineTransform
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.util.Matrix
import kotlin.math.roundToInt

class TextImagePainter(
    private val image: PDImageXObject,
    private val x: Float,
    private val y: Float,
    val imageWidth: Float,
    private val imageHeight: Float
) {
    fun drawImage(contentStream: PDPageContentStream) {
        val m = AffineTransform(imageWidth, 0f, 0f, imageHeight, x, y)
        contentStream.drawImage(image, Matrix(m))
    }

    companion object {
        fun create(document: PDDocument, text: String, textSize: Float, @ColorInt textColor: Int, x: Float, y: Float): TextImagePainter {
            check(text.isNotEmpty())

            val textPaint = TextPaint()
            textPaint.isAntiAlias = true
            textPaint.style = Paint.Style.FILL
            textPaint.textSize = textSize
            textPaint.color = textColor

            val descent = textPaint.fontMetrics.descent
            val realImageWidth = EmojiProcessor.measureText(text, textPaint)
            val realImageHeight = descent - textPaint.fontMetrics.ascent

            // そのままのテキストサイズで Bitmap に書き込むとサイズが小さくて表示が荒れてしまうので、テキストサイズを大きくして拡大した画像を作成する
            textPaint.textSize = textSize * 3
            // テキストの幅を算出
            val width = EmojiProcessor.measureText(text, textPaint).roundToInt()
            val height = textPaint.fontMetrics.let { it.descent - it.ascent }.roundToInt()

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            EmojiProcessor.drawTextToCanvas(text, canvas, textPaint)

            // PdfBox での画像形式に変換する
            val image = LosslessFactory.createFromImage(document, bitmap)
            return TextImagePainter(image = image, x = x, y = y, imageWidth = realImageWidth, imageHeight = realImageHeight)
        }
    }
}
