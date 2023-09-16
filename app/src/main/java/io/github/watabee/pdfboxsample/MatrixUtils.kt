package io.github.watabee.pdfboxsample

typealias PdfMatrix = com.tom_roush.pdfbox.util.Matrix
typealias GraphicMatrix = android.graphics.Matrix

/**
 * [android.graphics.Matrix] を [com.tom_roush.pdfbox.util.Matrix] に変換する.
 */
internal fun GraphicMatrix.toPdfMatrix(): PdfMatrix {
    val tmp = FloatArray(size = 9)
    // android.graphics.Matrix は一次元配列を使って以下のインデックスの値で3x3行列を表現している
    // 0 1 2
    // 3 4 5
    // 6 7 8
    getValues(tmp)

    // com.tom_roush.pdfbox.util.Matrix は一次元配列を使って以下のインデックスの値で3x3行列を表現している
    // 0 3 6
    // 1 4 7
    // 2 5 8
    return PdfMatrix(tmp[0], tmp[3], tmp[1], tmp[4], tmp[2], tmp[5])
}
