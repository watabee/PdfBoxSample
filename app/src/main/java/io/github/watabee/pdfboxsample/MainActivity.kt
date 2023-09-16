package io.github.watabee.pdfboxsample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import io.github.watabee.pdfboxsample.ui.theme.PdfBoxSampleTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private val pdfEditor: PdfEditor by lazy { PdfEditor(assetManager = assets) }
    private val dispatcher: CoroutineDispatcher by lazy { Executors.newSingleThreadExecutor().asCoroutineDispatcher() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PdfBoxSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var isEditing by remember { mutableStateOf(false) }

                    fun editPdf(block: (File) -> Unit) = runInBackground {
                        val destFile =
                            File(filesDir, "sample-${DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())}.pdf")
                        isEditing = true
                        block(destFile)
                        isEditing = false
                        openPdfFile(destFile)
                    }

                    Box {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            listItem(title = "Draw lines") {
                                editPdf(pdfEditor::drawLines)
                            }
                            listItem(title = "Draw transformed rect") {
                                editPdf(pdfEditor::drawTransformedRect)
                            }
                            listItem(title = "Draw texts") {
                                editPdf(pdfEditor::drawTexts)
                            }
                            listItem(title = "Draw transformed text") {
                                editPdf(pdfEditor::drawTransformedText)
                            }
                            listItem(title = "Draw japanese texts") {
                                editPdf(pdfEditor::drawJapaneseTexts)
                            }
                            listItem(title = "Draw multiple languages text") {
                                editPdf(pdfEditor::drawMultipleLanguagesText)
                            }
                            listItem(title = "Draw emoji text") {
                                editPdf(pdfEditor::drawEmojiText)
                            }
                        }

                        if (isEditing) {
                            Dialog(
                                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                                onDismissRequest = { }
                            ) {
                                Box(modifier = Modifier.padding(32.dp)) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openPdfFile(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/pdf")
            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(intent)
    }

    private inline fun runInBackground(crossinline block: suspend () -> Unit) {
        lifecycleScope.launch(dispatcher) { block() }
    }
}

private fun LazyListScope.listItem(title: String, onClick: () -> Unit) {
    item(key = title) {
        Column {
            ListItem(modifier = Modifier.fillMaxSize(), title = title, onClick = onClick)
            Divider()
        }
    }
}

@Composable
private fun ListItem(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}
