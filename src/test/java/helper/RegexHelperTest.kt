package helper

import helper.base.PrintHelper.StringTo.toPrintln
import helper.base.RegexHelper
import org.junit.Test
import org.w3c.dom.*
import javax.xml.parsers.DocumentBuilderFactory

internal class RegexHelperTest {
    @Test
    fun test1_RegexUtil() {
        val s = "        <Tab fx:id=\"mathTab\" text=\"mathView\">\n"
        val match = RegexHelper.oldMatch("<([A-Z][a-zA-Z]*) fx:id=\"([a-zA-Z_0-9]*)\"", s)
        println(match.toString())
    }

    @Test
    fun test2_() {
        fun matchString(input: String): Pair<String, Int>? {
            val regex = Regex("(\\w+)\\s*==\\s*(\\d+)")
            val matchResult = regex.find(input)
            return if (matchResult != null) {
                val a = matchResult.groupValues[1]
                val b = matchResult.groupValues[2].toInt()
                Pair(a, b)
            } else {
                null
            }
        }
        matchString("as == 1002").toString().toPrintln()
    }

    @Test
    fun test3_State() {
        fun t1(s: String) {
            RegexHelper.matchStateId(s).toString().toPrintln()
        }
        t1("<state id=\"switch_up\">")
        t1("<state id2=\"switch_up\">")
    }

    @Test
    fun t4() {
        //switch_up
        fun t1(s: String) {
            RegexHelper.match("switch_(\\w+)", s).let {
                if (it.find()) {
                    it.group(1).toPrintln()
                }
            }
        }
        t1("switch_up")
        t1("switch_down")
    }

    @Test
    fun t5() {
        // 创建一个 DocumentBuilderFactory 对象
        val factory = DocumentBuilderFactory.newInstance()

        // 使用工厂对象创建一个 DocumentBuilder 对象
        val builder = factory.newDocumentBuilder()

        // 使用 DocumentBuilder 对象解析 XML 文件，获取 Document 对象
        val doc = builder.parse("src/main/resources/scxml2/t_factory/t5.xml")

        // 获取文档的根元素
        val root = doc.documentElement

        // 输出根元素的名称和类型
        println("Node name: ${root.nodeName}")
        println("Node type: ${root.nodeType}")

        // 遍历子节点，输出其名称和类型
        val childNodes = root.childNodes
        for (i in 0 until childNodes.length) {
            val node = childNodes.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) {
                println("Node name: ${node.nodeName}")
                println("Node type: ${node.nodeType}")
            }
        }
    }

}