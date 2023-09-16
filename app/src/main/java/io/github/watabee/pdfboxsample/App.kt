package io.github.watabee.pdfboxsample

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        PDFBoxResourceLoader.init(applicationContext)
    }
}
