package helper.block

import helper.base.BaseTypeHelper.ListExpand.toArrayList
import helper.block.BlockHelper.Block
import helper.block.BlockHelper.Expand.BlockListTo.joinToAssignBracketBlock2
import helper.block.BlockHelper.Expand.BlockListTo.joinToBlock
import helper.block.BlockHelper.Expand.BlockListTo.joinToPrefixBracketBlock1
import helper.block.BlockHelper.Expand.BlockTo.toBracketBlock1
import helper.block.BlockHelper.Expand.BlockTo.toBracketBlock3
import helper.block.BlockHelper.Expand.BlockTo.toBracketBlock4
import helper.block.BlockHelper.Expand.BlockTo.toLineBlock
import helper.block.BlockHelper.Expand.BlockTo.toPrefixBracketBlock1
import helper.block.BlockHelper.Expand.ToAssignBlock.toAssignBlock
import helper.block.BlockHelper.Expand.ToBlock.toBlock
import helper.block.BlockHelper.language_scxml
import helper.block.LatexBlockHelper.LatexBlockFactory
import helper.block.ScxmlBlockHelper.ScxmlBlockFactory.Event.Companion.toBlocks
import java.util.*

object ScxmlBlockHelper {
    class ScxmlBlockFactory : LatexBlockFactory(
        language = language_scxml,
    ) {
        companion object {
            val bf = ScxmlBlockFactory()
        }

        interface LatexStringAble {
            fun toLatexBlock(): Block
            fun toLatexString(): String {
                return toLatexBlock().getStr()
            }
        }

        class Data(
            val id: String,
            val expr: String,
        ) : LatexStringAble {
            fun toFormalBlock(): Block {
                return bf.getArgAssignBlock(id, expr)
            }

            fun toSelfBlock(): Block {
                return toFormalBlock()
            }

            override fun toLatexBlock(): Block {
                return toSelfBlock()
            }
        }

        class DataModel(
            val dataList: ArrayList<Data> = ArrayList()
        ) : LatexStringAble {
            fun toFormalBlock(): Block {
                return dataList.map {
                    it.toFormalBlock()
                }.joinToBlock().toPrefixBracketBlock1()
            }

            fun toSelfBlock(): Block {
                return dataList.map {
                    it.toSelfBlock()
                }.joinToBlock().toPrefixBracketBlock1("dataModel")
            }

            override fun toLatexBlock(): Block {
                return toSelfBlock()
            }

            companion object {
                fun getTestDataModel(): DataModel {
                    return DataModel(
                        arrayListOf(
                            Data("d1", "1"),
                            Data("d2", "2"),
                        )
                    )
                }
            }
        }

        class Event(
            val name: String = "",
        ) : LatexStringAble {
            override fun toLatexBlock(): Block {
                return name.toBlock()
            }

            companion object {
                fun String.toEvent(): Event {
                    return Event(this)
                }

                fun ArrayList<String>.toEvents(): ArrayList<Event> {
                    return this.map {
                        it.toEvent()
                    }.toArrayList()
                }

                fun ArrayList<Event>.toBlocks(): ArrayList<Block> {
                    return this.map {
                        it.toLatexBlock()
                    }.toArrayList()
                }
            }
        }

        class Transition(
            val targetBlockState: BlockState,
            val events: ArrayList<Event> = ArrayList(),
        ) : LatexStringAble {
            override fun toLatexBlock(): Block {
                return bf.getEmptyBlock(
                    events.toBlocks().joinToBlock().toBracketBlock3(),
                    ",s".toBlock(),
                    targetBlockState.id.toBlock().toSubScriptBlock(),
                ).toBracketBlock1()
            }
        }

        class BlockState(
            var initialBlockState: BlockState? = null,
            var id: String = "",
            val onEntries: ArrayList<String> = arrayListOf(),
            val onExits: ArrayList<String> = arrayListOf(),
            val dataModel: DataModel = DataModel(),
            val transitions: ArrayList<Transition> = ArrayList(),
            val finalBlockStates: LinkedHashSet<BlockState> = linkedSetOf(),
            val children: LinkedHashSet<BlockState> = linkedSetOf(),
        ) : LatexStringAble {
            init {
                initialBlockState?.let {
                    children.add(it)
                }
                finalBlockStates.map {
                    children.add(it)
                }
            }

            fun bfsTraversal(
                init: (BlockState) -> Unit = {},
            ) {
                val queue = LinkedList<BlockState>()
                queue.add(this)

                while (queue.isNotEmpty()) {
                    val node = queue.poll()
                    init(node)
                    node.children.map {
                        queue.add(it)
                    }
                }
            }

            fun updateId(
                indexes: ArrayList<Int> = arrayListOf(0),
            ) {
                this.id = indexes.joinToString("")
                children.withIndex().map { (i, it) ->
                    it.updateId(indexes.plus(i).toArrayList())
                }
            }

            fun toSelfBlock(): Block {
                return arrayListOf(
                    this.id.toBlock().toAssignBlock("id"),
                ).joinToBlock().toPrefixBracketBlock1("state")
            }

            /**
             * 返回s_{0}的简略形式
             */
            fun toSelfLatexBlock(): Block {
                return bf.get01Block(
                    "s".toBlock(),
                    this.id.toBlock().toSubScriptBlock(),
                )
            }

            /**
             * 返回s_{0}的详细形式
             */
            override fun toLatexBlock(): Block {
                if (initialBlockState == null) {
                    return this.toSelfLatexBlock().toMathFormulaBlock().toLineBlock()
                }
                val superscript: String = (id.length - 1).toBlock().toSuperscriptBlock2().getStr()
                return bf.getEmptyBlock(
                    bf.getArgAssignBlock(
                        toSelfLatexBlock(),
                        arrayListOf(
                            "S${superscript}",
                            initialBlockState!!.toSelfLatexBlock().getStr(),
                            "F${superscript}",
                            "D${superscript}",
                            "T${superscript}",
                            "A${superscript}",
                        ).joinToString().toBlock().toBracketBlock1(),
                    ).toMathFormulaBlock().toLineBlock(),
                    bf.getNewLineBlock(),
                    bf.getArgAssignBlock(
                        "S${superscript}".toBlock(),
                        children.map {
                            it.toSelfLatexBlock()
                        }.joinToBlock().toBracketBlock1(),
                    ).toMathFormulaBlock().toLineBlock(),
                    bf.getNewLineBlock(),
                    bf.getArgAssignBlock(
                        "F${superscript}".toBlock(),
                        if (finalBlockStates.size == 0) {
                            bf.getEmptySetBlock()
                        } else {
                            finalBlockStates.map {
                                it.toSelfLatexBlock()
                            }.joinToBlock("").toBracketBlock4()
                        }
                    ).toMathFormulaBlock().toLineBlock(),
                    bf.getNewLineBlock(),
                    bf.getArgAssignBlock(
                        "D${superscript}".toBlock(),
                        if (dataModel.dataList.size == 0) {
                            bf.getEmptySetBlock()
                        } else {
                            dataModel.dataList.joinToString {
                                "${it.id}=${it.expr}"
                            }.toBlock().toBracketBlock1()
                        }
                    ).toMathFormulaBlock().toLineBlock(),
                    bf.getNewLineBlock(),
                    bf.getArgAssignBlock(
                        "T${superscript}".toBlock(),
                        if (transitions.size == 0) {
                            bf.getEmptySetBlock()
                        } else {
                            transitions.joinToString {
                                it.toLatexString()
                            }.toBlock().toBracketBlock4()
                        }
                    ).toMathFormulaBlock().toLineBlock(),
                    bf.getNewLineBlock(),
                    bf.getArgAssignBlock(
                        "A${superscript}".toBlock(),
                        if (onEntries.size == 0) {
                            bf.getEmptySetBlock()
                        } else {
                            onEntries.joinToString {
                                it
                            }.toBlock().toBracketBlock1()
                        }
                    ).toMathFormulaBlock().toLineBlock(),
                    bf.getNewLineBlock(),
                    "".toBlock().toLineBlock(),
                    children.map {
                        //若为AtomicState，则六元组内每个元素都为空或空集
                        it.toLatexBlock()
                    }.joinToBlock("\n\\\\\n"),
                )
            }

            override fun toString(): String {
                return "State(" +
                        "id='$id', " +
                        "dataModel='${dataModel.dataList}', " +
                        "initialState='${initialBlockState?.id}', " +
                        "children=$children" +
                        ")"
            }

            companion object {
                fun getTestState1(): BlockState {
                    val s000 = BlockState()
                    val s001 = BlockState()
                    val s00 = BlockState(
                        initialBlockState = s000,
                        finalBlockStates = linkedSetOf(s001),
                    )
                    val s01 = BlockState()
                    val s0 = BlockState(
                        initialBlockState = s00,
                        finalBlockStates = linkedSetOf(s01),
                    )
                    s0.updateId()
                    return s0
                }
            }
        }

        class Configuration(
            val blockStates: ArrayList<BlockState> = ArrayList(),
            var dataModel: DataModel = DataModel(),
        ) {
            fun toSelfBlock(): Block {
                return arrayListOf(
                    this.blockStates.map {
                        it.toSelfBlock()
                    }.joinToAssignBracketBlock2("states"),
                    this.dataModel.toSelfBlock(),
                ).joinToPrefixBracketBlock1("configuration")
            }

            fun toFormalBlock(): Block {
                return arrayListOf(
                    this.blockStates.map {
                        it.id.toBlock()
                    }.joinToBlock().toBracketBlock1(),
                    this.dataModel.toFormalBlock(),
                ).joinToBlock().toPrefixBracketBlock1("")
            }

            companion object {
                fun getTestConfiguration(): Configuration {
                    val configuration = Configuration()
                    configuration.blockStates.add(BlockState.getTestState1())
                    configuration.blockStates.add(BlockState.getTestState1())
                    configuration.dataModel = DataModel.getTestDataModel()
                    return configuration
                }
            }
        }
    }
}