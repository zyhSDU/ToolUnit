package helper

import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream

object OfficeHelper {
    interface DocXInterface

    object DocXPOIImpl : DocXInterface {
        fun readOneTable(file: File): ArrayList<ArrayList<String>> {
            val stringListList = ArrayList<ArrayList<String>>()
            XWPFDocument(FileInputStream(file)).run {
                tables[0].run {
                    rows.map {
                        val stringList = ArrayList<String>().apply {
                            stringListList.add(this)
                        }
                        it.tableCells.map {
                            stringList.add(it.text)
                        }
                    }
                }
            }
            return stringListList
        }
    }
}