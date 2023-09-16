package io.github.watabee.pdfboxsample

import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import androidx.core.graphics.scaleMatrix
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDFont
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import java.io.File
import kotlin.math.abs

class PdfEditor(private val assetManager: AssetManager) {
    private fun load(destFile: File, block: (PDDocument, PDPage, PDPageContentStream) -> Unit) {
        PDDocument.load(assetManager.open("sample.pdf")).use { document ->
            val pdPage = document.getPage(0)
            PDPageContentStream(
                document,
                pdPage,
                PDPageContentStream.AppendMode.APPEND, // PDFに上書きでコンテンツを表示したい場合は APPEND を設定する
                true,  // ページのコンテンツの圧縮設定
                true // グラフィックコンテキストをリセットするかどうか
                // PDFによって行列による変換処理が意図しない動作になることがあったため、trueにすることを推奨
            ).use { contentStream: PDPageContentStream ->
                block(document, pdPage, contentStream)
            }

            destFile.outputStream().buffered().use { outputStream ->
                document.save(outputStream)
            }
        }
    }

    fun drawLines(destFile: File) {
        load(destFile) { _, pdPage, contentStream ->
            // 線の色を設定
            contentStream.setStrokingColor(1.0f, 0.0f, 0.0f)
            // 線の太さを設定
            contentStream.setLineWidth(2.0f)

            val width = pdPage.mediaBox.width
            val height = pdPage.mediaBox.height
            contentStream.moveTo(0f, 0f)
            contentStream.lineTo(width / 2, height)
            contentStream.lineTo(width, 0f)

            contentStream.stroke()

            contentStream.setStrokingColor(0.0f, 0.0f, 1.0f)

            contentStream.moveTo(0f, height)
            contentStream.lineTo(width / 2, 0f)
            contentStream.lineTo(width, height)

            contentStream.stroke()
        }
    }

    fun drawTransformedRect(destFile: File) {
        load(destFile) { _, pdPage, contentStream ->
            // 線の色を設定
            contentStream.setStrokingColor(0.0f, 0.0f, 1.0f)
            // 矩形内の色を設定
            contentStream.setNonStrokingColor(1.0f, 0.0f, 0.0f)
            // 線の太さを設定
            contentStream.setLineWidth(4.0f)

            val w = 100f
            val h = 150f
            contentStream.addRect(0f, 0f, w, h)

            val m = Matrix()
            // 矩形の中心が原点に来るように移動する
            m.postTranslate(-w / 2, -h / 2)
            // 拡大する
            m.postScale(2f, 2f)
            // 回転する
            m.postRotate(30f)
            // 矩形の中心が PDF の中心に来るように移動する
            m.postTranslate(pdPage.mediaBox.width / 2, pdPage.mediaBox.height / 2)
            // 行列を適用する
            contentStream.transform(m.toPdfMatrix())
            contentStream.fillAndStroke() // 枠線+塗り
//            contentStream.fill() // 塗り
//            contentStream.stroke() // 枠線のみ
        }
    }

    fun drawTexts(destFile: File) {
        load(destFile) { _, pdPage, contentStream ->

            // テキスト編集処理の開始
            contentStream.beginText()

            // フォントとフォントサイズを設定する
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 32f)
            contentStream.setNonStrokingColor(1.0f, 0.0f, 0.0f)
            val fontMetrics1 = FontMetrics(PDType1Font.HELVETICA_BOLD, 32f)
            // テキストの描画位置を設定（フォントの descent の分だけずらして表示する）
            contentStream.newLineAtOffset(0f, -fontMetrics1.descent)
            contentStream.showText("Hello, PdfBox-Android!!!")

            val texts = "AAAAA\nBBBBB"
            contentStream.setFont(PDType1Font.TIMES_BOLD, 64f)
            contentStream.setNonStrokingColor(0.0f, 1.0f, 0.0f)
            contentStream.newLineAtOffset(pdPage.mediaBox.width / 2, pdPage.mediaBox.height / 2)
            val fontMetrics2 = FontMetrics(PDType1Font.TIMES_BOLD, 64f)
            // 改行分の高さを設定する（ここではフォントの高さと同じに設定）
            contentStream.setLeading(fontMetrics2.height)
            // 改行文字が含まれる場合、改行文字ごとに分割してテキストを表示する
            texts.split("\n").forEach { text ->
                contentStream.showText(text)
                // 改行する
                contentStream.newLine()
            }

            // テキスト編集処理の終了
            contentStream.endText()
        }
    }

    fun drawTransformedText(destFile: File) {
        load(destFile) { _, pdPage, contentStream ->

            val text = "Hello, PdfBox-Android!!!"
            val fontSize = 32f
            // テキスト編集処理の開始
            contentStream.beginText()

            // フォントとフォントサイズを設定する
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize)
            contentStream.setNonStrokingColor(1.0f, 0.0f, 0.0f)
            val fontMetrics = FontMetrics(PDType1Font.HELVETICA_BOLD, fontSize)

            val textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(text) * fontSize / 1000
            val textHeight = fontMetrics.height

            val m = Matrix()
            m.postTranslate(-textWidth / 2, -textHeight / 2)
            m.postRotate(135f)
            m.postTranslate(pdPage.mediaBox.width / 2, pdPage.mediaBox.height / 2)

            contentStream.setTextMatrix(m.toPdfMatrix())
            // テキストの描画位置を設定（フォントの descent の分だけずらして表示する）
            contentStream.newLineAtOffset(0f, -fontMetrics.descent)
            contentStream.showText(text)

            // テキスト編集処理の終了
            contentStream.endText()
        }
    }

    fun drawJapaneseTexts(destFile: File) {
        load(destFile) { pdDocument, pdPage, contentStream ->

            // 日本語のフォントをロードする
            val font = PDType0Font.load(pdDocument, assetManager.open("NotoSansJP-Regular.ttf"))
            val fontSize = 32f
            val fontMetrics = FontMetrics(font, fontSize)

            contentStream.beginText()
            contentStream.setFont(font, fontSize)
            contentStream.setNonStrokingColor(1.0f, 0.0f, 0.0f)
            contentStream.newLineAtOffset(0f, -fontMetrics.descent)
            contentStream.showText("こんにちは, PdfBox-Android!!!")

            contentStream.setNonStrokingColor(0.0f, 1.0f, 0.0f)
            contentStream.newLineAtOffset(pdPage.mediaBox.width / 2, pdPage.mediaBox.height / 2)
            contentStream.showText("あいうえお")
            // 改行するために setLeading を設定する必要がある
            contentStream.setLeading(fontMetrics.height)
            // 改行する
            contentStream.newLine()

            contentStream.setNonStrokingColor(0.0f, 0.0f, 1.0f)
            contentStream.showText("かきくけこ")

            contentStream.endText()
        }
    }

    fun drawMultipleLanguagesText(destFile: File) {
        load(destFile) { pdDocument, pdPage, contentStream ->

            // 日本語のフォントをロードする
            val jpFont = PDType0Font.load(pdDocument, assetManager.open("NotoSansJP-Regular.ttf"))
            // タイ語のフォントをロードする（システムのフォントを参照する）
            val thaiFont = PDType0Font.load(pdDocument, File("/system/fonts/NotoSansThaiUI-Regular.ttf"))
            val fonts = listOf(jpFont, thaiFont)
            val fontSize = 32f
            val jpFontMetrics = FontMetrics(jpFont, fontSize)
            val texts = "こんにちは、สวัสดี\nปลาแมคเคอเรลอัตกะ、\uD867\uDE3D" // \uD867\uDE3D で 𩸽

            // 改行文字ごとに分割してテキストを出力する
            texts.split("\n").forEachIndexed { index, text ->
                contentStream.beginText()
                contentStream.setNonStrokingColor(1.0f, 0.0f, 0.0f)
                contentStream.newLineAtOffset(0f, pdPage.mediaBox.height / 2 - index * jpFontMetrics.height)

                text.forEach { }
                // テキストを見た目の文字ごとに PDF に出力する
                text.divideByGraphemeCluster().forEach { s ->
                    // 1文字ごとに日本語とタイ語で出力できるかどうかを試す
                    for (font in fonts) {
                        try {
                            contentStream.setFont(font, fontSize)
                            contentStream.showText(s)

                            // 1文字ごとに文字幅を取得し、次の表示位置を設定する
                            val width = font.getStringWidth(s) * fontSize / 1000
                            contentStream.newLineAtOffset(width, 0f)
                            break
                        } catch (e: IllegalArgumentException) {
                            Log.w("PdfEditor", "テキスト出力失敗: $s")
                        }
                    }
                }

                contentStream.endText()
            }
        }
    }

    fun drawEmojiText(destFile: File) {
        load(destFile) { pdDocument, _, contentStream ->

            // 日本語のフォントをロードする
            val font = PDType0Font.load(pdDocument, assetManager.open("NotoSansJP-Regular.ttf"))
            val fontSize = 32f
            val fontMetrics = FontMetrics(font, fontSize)
            val text = """こんにちは😀안녕하세요🫠あいうえお"""

            contentStream.beginText()
            contentStream.setFont(font, fontSize)
            contentStream.setNonStrokingColor(1.0f, 0.0f, 0.0f)
            contentStream.newLineAtOffset(0f, -fontMetrics.descent)

            val textImagePainters = mutableListOf<TextImagePainter>()
            var totalWidth = 0f
            text.divideByGraphemeCluster().forEach { s ->
                val width = try {
                    contentStream.showText(s)

                    font.getStringWidth(s) * fontSize / 1000
                } catch (e: IllegalArgumentException) {
                    Log.w("PdfEditor", "テキスト出力失敗: $s")

                    // テキストの出力に失敗した場合、画像として出力させるようにする
                    val textImagePainter =
                        TextImagePainter.create(pdDocument, text = s, textSize = fontSize, textColor = Color.RED, x = totalWidth, y = 0f)
                    // beginText ~ endText までの間に画像描画処理を呼び出すと例外が投げられるため、画像の描画に必要な情報を配列に入れておく
                    textImagePainters.add(textImagePainter)

                    textImagePainter.imageWidth
                }

                contentStream.newLineAtOffset(width, 0f)
                totalWidth += width
            }

            contentStream.endText()

            // テキスト表示処理が終わったので、画像の描画処理を行う.
            textImagePainters.forEach {
                it.drawImage(contentStream)
            }
        }
    }
}

private data class FontMetrics(
    val ascent: Float,
    val descent: Float
) {
    constructor(font: PDFont, fontSize: Float) : this(
        font.fontDescriptor.ascent * fontSize / 1000,
        font.fontDescriptor.descent * fontSize / 1000,
    )

    val height: Float = abs(descent - ascent)
}
