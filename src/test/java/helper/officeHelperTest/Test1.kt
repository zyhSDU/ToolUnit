package helper.officeHelperTest

import model.StringNode
import org.junit.Test
import res.FileRes
import helper.OfficeHelper
import java.io.File

open class Test1 {
    @Test
    fun t1() {
        val filePrefix = "0092测试用例_15"
        val file1 = File(FileRes.d_a2_File, "${filePrefix}.docx")
        val file2 = File(FileRes.d_a2_File, "${filePrefix}.txt")
        val readOneTable = OfficeHelper.DocXPOIImpl.readOneTable(file1)
        /*
        readOneTable.withIndex().map { (i, it) ->
            println("line${i}:")
            it.withIndex().map { (i, it) ->
                println("\tcell${i}:")
                println("\t\t${it}")
            }
        }
         */
        val nodeArrayList1 = StringNode.form(readOneTable)
        StringNode.printList(nodeArrayList1)
//        StringNode.saveList(nodeArrayList1,file2)
    }
}