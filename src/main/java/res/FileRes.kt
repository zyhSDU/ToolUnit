package res

import java.io.File

object FileRes {
    val root = File(File("").absolutePath)
    val document = File(root, "document")
    val d_a1 = File(document, "a1")
    val d_a2 = File(document, "a2")

    @JvmStatic
    fun main(args: Array<String>) {
        println(root.absolutePath)
        println(document.absolutePath)
        println(d_a1.absolutePath)
        println(d_a2.absolutePath)
    }
}