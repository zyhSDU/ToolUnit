package helper.blockHelperTest

import helper.base.PrintHelper.StringTo.toPrint
import helper.block.BlockHelper
import helper.block.BlockHelper.Expand.ToBlock.toBlock
import helper.block.PyBlockHelper
import org.junit.Test

internal class PyTest {
    val bf = PyBlockHelper.PyBlockFactory.bf

    @Test
    fun pyTask1() {
        fun getListBlockString(
            arr_name: String = "b",
            bs_size: Int = 16,
        ): String {
            val sb = StringBuilder()
            fun getAssignBBlock(index: Int): BlockHelper.Block {
                return bf.getLineBlock(
                    bf.getArgAssignBlock(
                        "${arr_name}${index}".toBlock(),
                        "${arr_name}s[${index}]".toBlock(),
                    )
                )
            }

            var ifLStrip = true
            (0 until bs_size).map {
                if (it > 0) {
                    ifLStrip = false
                }
                val str = getAssignBBlock(it).getStr(ifLStrip = ifLStrip)
                sb.append(str)
            }
            val bsStr = arrayListOf<BlockHelper.Block>()
            (0 until bs_size).map {
                bsStr.add("${arr_name}${it}".toBlock())
            }

            val argsBlock = bf.getArgsBlock(bsStr)

            val str = bf.getLineBlock(
                bf.getEmptyBlock(
                    "# from helper.coder.Replacer import ${arr_name}s, ".toBlock(),
                    argsBlock,
                )
            ).getStr(ifLStrip = false)
            sb.append(str)
            return sb.toString()
        }

        val listBlockString = getListBlockString("w", 16)
        listBlockString.toPrint()
        val assertResult = """w0 = ws[0]
w1 = ws[1]
w2 = ws[2]
w3 = ws[3]
w4 = ws[4]
w5 = ws[5]
w6 = ws[6]
w7 = ws[7]
w8 = ws[8]
w9 = ws[9]
w10 = ws[10]
w11 = ws[11]
w12 = ws[12]
w13 = ws[13]
w14 = ws[14]
w15 = ws[15]
# from helper.coder.Replacer import ws, w0, w1, w2, w3, w4, w5, w6, w7, w8, w9, w10, w11, w12, w13, w14, w15"""
        assert(listBlockString == assertResult)
    }

    @Test
    fun pyTask2() {
        val cb = bf.getInitFunDefineBlock(
            bBlocks = arrayListOf(
                bf.getLineBlock("a=1".toBlock())
            ),
        )
        val str = cb.getStr()
        str.toPrint()
        val assertResult = "def __init__(\n" +
                "\tself,\n" +
                ")->None:\n" +
                "\ta=1"
        assert(str == assertResult)
    }

    @Test
    fun pyTask3() {
        val cb = bf.getBaseClassDefineBlock(
            class_name = "BBB",
            father_class_name = "object",
            args = arrayListOf(
                BlockHelper.ArgUnit("name", "str", "\"\"")
            ),
        )
        val str = cb.getStr()
        str.toPrint()
        val assertResult = """class BBB(object):
	def __init__(
		self,
		name: str = "",
	)->None:
		self.name : str = name"""
        assert(str == assertResult)
    }
}