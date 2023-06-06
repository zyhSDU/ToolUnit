package helper

import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object DocHelper {
    @JvmStatic
    fun main(args: Array<String>) {
        File(File("").absolutePath, "document\\a1\\新建 Microsoft Word 文档.docx").apply {
            readDocx(this).apply {
                println(this)
            }
        }
    }

    fun readDocx(file: File): String {
        var text = ""
        try {
            val doc = XWPFDocument(FileInputStream(file))
            val extractor = XWPFWordExtractor(doc)
            text = extractor.text
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return text
    }
}