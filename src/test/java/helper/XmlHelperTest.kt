package helper

import org.junit.Test
import java.io.File
import java.io.PrintWriter

internal class XmlHelperTest {
    private fun replaceTask1(resDir: String) {
        val inputFile = File("${resDir}t1.xml")
        val outputFile = File("${resDir}t2.xml")

        outputFile.printWriter().use { writer: PrintWriter ->
            inputFile.forEachLine { line ->
                // 注意
                // label标签必须在同一行
                // label标签不可太长超过256
                val pattern_label = Regex("((?<=<label.{0,256}?>)(.*?)(?=</label>))")
                // formula标签同理
                val pattern_formula = Regex("((?<=<formula.{0,256}?>)(.*?)(?=</formula>))")
                // comment标签同理
                val pattern_comment = Regex("((?<=<comment.{0,256}?>)(.*?)(?=</comment>))")
                // pattern汇总
                val pattern = Regex("${pattern_label.pattern}|${pattern_formula.pattern}|${pattern_comment.pattern}")
                val output = pattern.replace(line) {
                    it.value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                }
                writer.println(output)
            }
        }
        println("转义完成")
    }

    @Test
    fun t1() {
        val resDir = "D:\\Users\\zyh\\GitProject\\ToolUnit\\src\\main\\resources\\uppaal\\model\\jobshop_smc\\"
        replaceTask1(resDir)
    }

    @Test
    fun findTagInString1() {
        val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <root>
            <queries>
                <query>
                    SELECT * FROM users
                    <formula>x * 2</formula>
                    <comment>This is a comment for users</comment>
                </query>
                <query>
                    SELECT * FROM orders
                    <formula>y + 5</formula>
                    <comment>This is a comment for orders</comment>
                </query>
            </queries>
            <data>
                <item>Item 1</item>
                <item>Item 2</item>
            </data>
        </root>
    """.trimIndent()
        val tagHierarchy = XmlHelper.TagNode(
            "queries", listOf(
                XmlHelper.TagNode(
                    "query",
                    listOf(
                        XmlHelper.TagNode("formula"),
                        XmlHelper.TagNode("comment"),
                    )
                )
            )
        )
        XmlHelper.findTagInString(xml, tagHierarchy) {
            println(it)
        }
    }

    @Test
    fun findTagInString2() {
        val resDir = "D:\\Users\\zyh\\GitProject\\ToolUnit\\src\\main\\resources\\uppaal\\model\\jobshop_smc\\"
        val xml = File("${resDir}model.xml").readText()
        val outputFile = File("${resDir}query.q")

        val tagHierarchy = XmlHelper.TagNode(
            "queries", listOf(
                XmlHelper.TagNode(
                    "query", listOf(
                        XmlHelper.TagNode("formula"),
                        XmlHelper.TagNode("comment", outputPrefix = "// "),
                    )
                ),
            )
        )
        outputFile.printWriter().use { writer: PrintWriter ->
            XmlHelper.findTagInString(xml, tagHierarchy) {
                writer.println(it)
            }
        }
    }
}
