package helper.block

import helper.block.BlockHelper.ArgUnit
import helper.block.BlockHelper.Block
import helper.block.BlockHelper.BlockFactory
import helper.block.BlockHelper.Expand.ToBlock.toBlock
import helper.block.BlockHelper.b0
import helper.block.BlockHelper.b1
import helper.block.BlockHelper.b2
import helper.block.BlockHelper.b3
import helper.block.BlockHelper.language_py
import helper.block.BlockHelper.w0
import helper.block.BlockHelper.w1

object PyBlockHelper {

    class PyBlockFactory : BlockFactory(
        language = language_py,
    ) {
        fun getArrBlock(
            v: Block,
        ) = getBlock(
            template = "[$b0]",
            bBlocks = arrayListOf(
                v,
            ),
        )

        fun getFunDefineBlock(
            return_type: String,
            method_name: String,
            args: ArrayList<ArgUnit> = arrayListOf(),
            bBlocks: ArrayList<Block> = ArrayList(),
        ): Block {
            val argsBlock = getEmptyBlock()
            args.map {
                argsBlock.addBlock(it.getArgBlock())
            }
            return getBlock(
                template = "\ndef $b0(" +
                        "$w0$b1" +
                        "\n)->$b2:" +
                        "$w1$b3",
                arrayListOf(
                    method_name.toBlock(),
                    argsBlock,
                    return_type.toBlock(),
                    getEmptyBlock(bBlocks),
                ),
            )
        }

        fun getInitFunDefineBlock(
            args: ArrayList<ArgUnit> = arrayListOf(),
            bBlocks: ArrayList<Block> = ArrayList(),
        ): Block {
            args.add(0, ArgUnit("self"))
            return getFunDefineBlock(
                "None",
                "__init__",
                args,
                bBlocks,
            )
        }

        fun getBaseClassDefineBlock(
            class_name: String,
            father_class_name: String,
            args: ArrayList<ArgUnit> = arrayListOf(),
        ): Block {
            val replaceList = arrayListOf<Block>()
            args.map {
                replaceList.add(it.getSelfAssignBlock())
            }
            return getBlock(
                template = "\nclass $class_name(${father_class_name}):" +
                        "$w0$b0",
                bBlocks = arrayListOf(
                    getInitFunDefineBlock(
                        args = args,
                        bBlocks = replaceList,
                    ),
                ),
            )
        }

        companion object {
            val bf = PyBlockFactory()
        }
    }
}