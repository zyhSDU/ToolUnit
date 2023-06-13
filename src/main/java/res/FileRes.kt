package res

import java.io.File

object FileRes {
    val root_file = File(File("").absolutePath)
    val document_file = File(root_file, "document")
    val d_a1_File = File(document_file, "a1")
    val d_a2_File = File(document_file, "a2")
    val out_file = File(root_file, "out")
    val out_chart_file = File(out_file, "chart")
}