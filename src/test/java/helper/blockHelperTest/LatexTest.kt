package helper.blockHelperTest

import helper.base.FileHelper
import helper.base.TextHelper.toTextFile
import helper.base.TimeHelper
import helper.block.BlockHelper.Expand.ToBlock.toLineBlock
import helper.block.LatexBlockHelper
import helper.blockHelperTest.LatexTest.TextElement.Companion.temp0
import helper.blockHelperTest.LatexTest.TextElement.Companion.temp1
import org.junit.Test

internal class LatexTest {
    @Test
    fun testString() {
        var s1 = "111"
        val s2 = s1
        s1 += "222"
        println(s1)
        println(s2)
    }

    val bf = LatexBlockHelper.LatexBlockFactory.bf

    data class TextElement(
        val s0: String,
        var s1: String = s0,
        var s2: String = s1,
    ) {
        init {
            s1 = s1.replace(temp0, s0)
            s2 = s2.replace(temp0, s0).replace(temp1, s1)
        }

        companion object {
            val temp = "temp"
            val temps = (0..1).map {
                "__$temp${it}__"
            }
            val temp0 = temps[0]
            val temp1 = temps[1]
        }
    }

    @Test
    fun test1() {
        val nonNegativeRealSetElement = TextElement(
            "R _ { \\geq 0 }",
            "\$$temp0\$",
            "非负实数集合$temp1"
        )

        val scxmlAutomatonElement = TextElement("scxml自动机")
        val scxmlAutomatonYuanZuShuElement = TextElement("四元组")

        val documentBlock = bf.getTexBlock(
            packages = arrayListOf("ctex"),
            title = "scxml",
            author = "张宇涵",
            date = "March 2023",
            documentContentBlock = bf.getDocumentBlockBlock(
                bf.getSectionBlock(
                    "Introduction",
                    bf.getEmptyBlock(
                        ".".toLineBlock(),
                        "\\\\".toLineBlock(),
                        nonNegativeRealSetElement.s2.toLineBlock(),
                        "\\\\".toLineBlock(),
                        "一个${scxmlAutomatonElement.s0}是一个${scxmlAutomatonYuanZuShuElement.s0}".toLineBlock(),
                    )
                )
            ),
        )
        val fileName = "log/scxml/scxml_${TimeHelper.now(TimeHelper.TimePattern.p4)}.txt"
        FileHelper.createFileIfNotExists(fileName)
        documentBlock.getStr().toTextFile(fileName)
    }
}