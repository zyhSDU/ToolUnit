package helper.base

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

object CopyHelper {
    fun getFromClipboard(): String {
        var result = ""
        Toolkit.getDefaultToolkit().systemClipboard.getContents(null)?.apply {
            if (this.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    result = this.getTransferData(DataFlavor.stringFlavor) as String
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    fun String.copyToClipboard() {
        StringSelection(this).let {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(it, it)
        }
    }

    fun StringBuilder.copyToClipboard() {
        this.toString().copyToClipboard()
    }
}