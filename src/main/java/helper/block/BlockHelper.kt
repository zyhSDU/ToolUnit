package helper.block

import helper.base.BaseTypeHelper.ListExpand.toArrayList
import helper.base.BaseTypeHelper.toInt
import helper.base.StringHelper.StringTo.to01String
import helper.block.BlockHelper.Expand.BlockListTo.joinToBlock
import helper.block.BlockHelper.Expand.BlockTo.toAssignBracketBlock2
import helper.block.BlockHelper.Expand.BlockTo.toBracketBlock3
import helper.block.BlockHelper.Expand.BlockTo.toLineBlock
import helper.block.BlockHelper.Expand.BlockTo.toPrefixBracketBlock1
import helper.block.BlockHelper.Expand.ToBlock.toBlock
import java.io.File

object BlockHelper {
    val strB: String = "b"
    val bs: ArrayList<String> = arrayListOf<String>().apply {
        (0 until 16).map {
            this.add("__$strB${it}__")
        }
    }
    val b0 = bs[0]
    val b1 = bs[1]
    val b2 = bs[2]
    val b3 = bs[3]

    val strWrappedBlockMark = "w"
    val ws: ArrayList<String> = arrayListOf<String>().apply {
        (0 until 16).map {
            this.add("__$strWrappedBlockMark${it}__")
        }
    }
    val w0 = ws[0]
    val w1 = ws[1]

    data class Language(
        val name: String = "",
        val remark_prefix: String = "",
        val line_end: String = "",
    ) {
        override fun toString(): String {
            return name
        }
    }

    val language_text = Language()
    val language_c = Language("c", "//", ";")
    val language_cpp = Language("cpp", "//", ";")
    val language_py = Language("py", "# ")
    val language_latex = Language("language_latex")
    val language_ta = Language("timed automata", "// ")
    val language_scxml = Language("scxml", "// ")

    data class Block(
        val language: Language = language_text,
        var template: String = "",
        val bBlocks: ArrayList<Block> = ArrayList(),
    ) {
        val wBlocks: ArrayList<Block> = ArrayList()

        init {
            var iWrappedBlock = 0
            (0 until bBlocks.size).map { i ->
                if (iWrappedBlock >= 2) return@map
                val reNew = bs[i]
                val reOld = "${ws[iWrappedBlock]}${reNew}"
                if (this.template.contains(reOld)) {
                    this.wBlocks.add(this.bBlocks[i])
                    iWrappedBlock += 1
                    this.template = this.template.replace(reOld, reNew)
                }
            }
        }

        fun ifContainI(
            index: Int,
        ): Boolean {
            return this.template.contains(bs[index])
        }

        fun getStr(
            tab_num: Int = 0,
            ifLStrip: Boolean = true,
        ): String {
            val nowTabStr = "\t".repeat(tab_num)
            var res = this.template
            res = res.replace("\n", "\n${nowTabStr}")
            var ifBeginNotContains = false
            this.bBlocks.withIndex().map { (i, v) ->
                var ifContainI = false
                if (!ifBeginNotContains) {
                    ifContainI = this.ifContainI(i)
                    if (!ifContainI) {
                        ifBeginNotContains = true
                    }
                }
                val vStr = if (v.bBlocks.size == 0) {
                    if (this.wBlocks.contains(v)) {
                        v.template.replace("\n", "\n${nowTabStr}")
                    } else {
                        v.template
                    }
                } else {
                    v.getStr(
                        tab_num = tab_num + ifContainI.toInt(),
                        ifLStrip = false,
                    )
                }
                if (ifContainI) {
                    res = res.replace(bs[i], vStr)
                } else {
                    res += vStr
                }
            }
            if (ifLStrip) {
                res = res.removePrefix("\n")
            }
            return res
        }

        override fun toString(): String {
            return this.getStr()
        }

        fun printCode(
            file: File? = null,
            ifLStrip: Boolean = true,
        ) {
            val str = this.getStr(ifLStrip = ifLStrip)
            file?.run {
                writeText(str)
            } ?: run {
                print(str)
            }
        }

        fun addBlock(
            vararg bBlocks: Block,
        ) {
            bBlocks.map {
                this.bBlocks.add(it)
            }
        }

        fun addLineBlock() {
            this.addBlock("\n".toBlock())
        }
    }

    class ArgUnit(
        val argName: String,
        val argType: String = "",
        val argDefaultValue: String = "",
    ) {
        init {
            assert(argName.isNotBlank())
        }

        fun getArgBlock(): Block {
            val sb = StringBuilder()
            sb.append("\n$argName")
            if (argType != "") {
                sb.append(": $argType")
            }
            if (argDefaultValue != "") {
                sb.append(" = $argDefaultValue")
            }
            sb.append(",")
            return sb.toString().toBlock()
        }

        fun getSelfAssignBlock(): Block {
            val sb = StringBuilder()
            sb.append("\nself.$argName")
            if (argType != "") {
                sb.append(" : $argType")
            }
            sb.append(" = $argName")
            return sb.toString().toBlock()
        }
    }

    open class BlockFactory(
        val language: Language = language_text,
    ) {
        //必须有
        fun getBlock(
            template: String,
            bBlocks: ArrayList<Block> = ArrayList(),
        ): Block {
            return Block(
                this.language,
                template,
                bBlocks,
            )
        }

        //简化调用
        fun getBlock(
            template: String,
            vararg bBlocks: Block,
        ): Block {
            return getBlock(
                template,
                bBlocks.toArrayList(),
            )
        }

        //简化调用
        fun getBlock(
            template: String,
            vararg bBlocks: String,
        ): Block {
            return getBlock(
                template,
                bBlocks.map { it.toBlock() }.toArrayList()
            )
        }

        //必须有
        fun getEmptyBlock(
            bBlocks: ArrayList<Block> = ArrayList(),
        ): Block {
            return this.getBlock(
                "",
                bBlocks,
            )
        }

        //简化调用
        fun getEmptyBlock(
            vararg replaceList: Block,
        ): Block {
            return this.getEmptyBlock(
                replaceList.toArrayList(),
            )
        }

        fun get01Block(
            a0: Block,
            a1: Block,
        ): Block {
            return getBlock(
                template = "$b0$b1",
                bBlocks = arrayListOf(
                    a0,
                    a1,
                ),
            )
        }

        fun get010Block(
            a0: Block,
            a1: Block,
        ): Block {
            return getBlock(
                template = "$b0$b1$b0",
                bBlocks = arrayListOf(
                    a0,
                    a1,
                ),
            )
        }

        fun get012Block(
            a0: Block,
            a1: Block,
            a2: Block,
        ): Block {
            return getBlock(
                template = "$b0$b1$b2",
                bBlocks = arrayListOf(
                    a0,
                    a1,
                    a2,
                ),
            )
        }

        fun getLineBlock(
            v: Block = "".toBlock(),
            line_end: String = "",
        ): Block {
            return this.getBlock(
                template = "\n$b0${line_end}",
                bBlocks = arrayListOf(
                    v,
                ),
            )
        }

        fun getArgsBlock(
            bBlocks: ArrayList<Block> = ArrayList(),
        ): Block {
            return getBlock(
                template = bs.slice(0 until bBlocks.size).joinToString(),
                bBlocks = bBlocks,
            )
        }

        fun getArgAssignBlock(
            k: Block,
            v: Block,
        ) = getBlock(
            template = "$b0 = $b1",
            bBlocks = arrayListOf(
                k,
                v,
            ),
        )

        fun getArgAssignBlock(
            k: String,
            v: String,
        ): Block {
            return getArgAssignBlock(
                k.toBlock(),
                v.toBlock(),
            )
        }

        fun getBoolBlock(
            k: Block,
            e: Block,
            v: Block,
        ): Block {
            return get012Block(k, e, v)
        }

        fun getBoolLeBlock(
            k: Block,
            v: Block,
        ): Block {
            return getBoolBlock(k, "<".toBlock(), v)
        }

        fun getFunCallBlock(
            method_name: String,
            args: Block,
            if_line: Boolean = true,
        ): Block {
            val cb = getBlock(
                template = "$b0($b1)",
                bBlocks = arrayListOf(
                    method_name.toBlock(),
                    args,
                ),
            )
            return if (!if_line) {
                cb
            } else {
                getLineBlock(
                    cb,
                    this.language.line_end,
                )
            }
        }

        fun getARemarkBlock(
            content: String,
        ): Block = language.remark_prefix.to01String(content).toBlock()

        fun getRemarkBlock(
            bBlocks: ArrayList<String> = arrayListOf(),
        ): Block {
            val listLen = bBlocks.size
            if (
                listLen == 0 ||
                (listLen == 1 && "" == bBlocks[0])
            ) {
                return "".toBlock()
            }
            val cb = getEmptyBlock()
            bBlocks.map {
                cb.addBlock(getARemarkBlock(it))
            }
            return cb
        }
    }

    val bf = BlockFactory()


    object Expand {
        object ToBlock {
            fun String.toBlock(): Block {
                return bf.getBlock(this)
            }

            fun Int.toBlock(): Block {
                return this.toString().toBlock()
            }

            fun Float.toBlock(): Block {
                return this.toString().toBlock()
            }

            fun String.toLineBlock(): Block {
                return this.toBlock().toLineBlock()
            }

            fun HashSet<String>.toBlock(): Block {
                return this.toList().joinToString(",").toBlock()
            }
        }

        object ToAssignBlock {
            fun String.toAssignBlock(argName: String): Block {
                return bf.getArgAssignBlock(
                    argName,
                    this,
                )
            }

            fun Block.toAssignBlock(argName: String): Block {
                return bf.getArgAssignBlock(
                    argName.toBlock(),
                    this,
                )
            }
        }

        object BlockTo {
            fun Block.toLineBlock(
                line_end: String = "",
            ): Block {
                return bf.get012Block(
                    "\n".toBlock(),
                    this,
                    line_end.toBlock(),
                )
            }

            fun Block.toBracketBlock(
                left: String,
                right: String = left,
            ): Block {
                return bf.get012Block(
                    left.toBlock(),
                    this,
                    right.toBlock(),
                )
            }

            fun Block.toBracketBlock1(): Block {
                return this.toBracketBlock("(", ")")
            }

            fun Block.toBracketBlock2(): Block {
                return this.toBracketBlock("[", "]")
            }

            fun Block.toBracketBlock3(): Block {
                return this.toBracketBlock("{", "}")
            }

            fun Block.toBracketBlock4(): Block {
                return this.toBracketBlock("\\{", "\\}")
            }

            fun Block.toPrefixBlock(
                prefixString: String,
            ): Block {
                return bf.get01Block(
                    prefixString.toBlock(),
                    this,
                )
            }

            fun Block.toPrefixBracketBlock1(
                prefixString: String = "",
            ): Block {
                return this.toBracketBlock1().toPrefixBlock(prefixString)
            }

            fun Block.toPrefixBracketBlock2(
                prefixString: String = "",
            ): Block {
                return this.toBracketBlock2().toPrefixBlock(prefixString)
            }

            fun Block.toAssignBracketBlock2(
                argName: String,
            ): Block {
                return bf.getArgAssignBlock(
                    argName.toBlock(),
                    this.toBracketBlock2(),
                )
            }
        }

        object BlockListTo {
            fun List<Block>.joinToBlock(
                separator: CharSequence = ", ",
            ): Block {
                return this.joinToString(separator) {
                    it.getStr()
                }.toBlock()
            }

            fun List<Block>.joinToPrefixBracketBlock1(
                argName: String,
            ): Block {
                return this.joinToBlock().toPrefixBracketBlock1(argName)
            }

            fun List<Block>.joinToAssignBracketBlock2(
                argName: String,
            ): Block {
                return this.joinToBlock().toAssignBracketBlock2(argName)
            }
        }

        object ToBlockList {
            fun ArrayList<String>.toBlockArrayList(): ArrayList<Block> {
                val res = arrayListOf<Block>()
                map {
                    res.add(it.toBlock())
                }
                return res
            }
        }

        object LHMExpand {

            fun LinkedHashMap<String, Double>.toBlock(): BlockHelper.Block {
                return this.map { (k, v) ->
                    "${k}: $v".toBlock()
                }.joinToBlock().toBracketBlock3()
            }
        }
    }
}