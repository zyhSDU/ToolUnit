package helper.block

import helper.block.BlockHelper.Block
import helper.block.BlockHelper.BlockFactory
import helper.block.BlockHelper.Expand.ToBlock.toBlock
import helper.block.BlockHelper.b0
import helper.block.BlockHelper.b1
import helper.block.BlockHelper.b2
import helper.block.BlockHelper.b3
import helper.block.BlockHelper.language_c
import helper.block.BlockHelper.w0

object CBlockHelper {
    class CBlockFactory : BlockFactory(
        language = language_c,
    ) {
        fun getIncludeBlock(
            lib_name: String,
        ) = getLineBlock(
            getBlock(
                template = "#include $b0",
                bBlocks = arrayListOf(
                    lib_name.toBlock(),
                ),
            ),
        )

        fun getArgBlock(
            arg_type: String,
            arg_name: Block,
        ) = getBlock(
            template = "$b0 $b1",
            bBlocks = arrayListOf(
                arg_type.toBlock(),
                arg_name,
            ),
        )

        fun getArgBlock(
            arg_type: String,
            arg_name: String,
        ) = getArgBlock(
            arg_type,
            arg_name.toBlock(),
        )

        fun getArgDeclareBlock(
            arg_type: String,
            arg_name: Block,
        ) = getLineBlock(
            getBlock(
                template = "$b0 $b1;",
                bBlocks = arrayListOf(
                    arg_type.toBlock(),
                    arg_name,
                ),
            )
        )

        fun getArgDeclareBlock(
            arg_type: String,
            arg_name: String,
        ) = getArgDeclareBlock(
            arg_type,
            arg_name.toBlock(),
        )

        fun getDefineBlock(
            arg_k: String,
            arg_v: String,
        ) = getLineBlock(
            getBlock(
                template = "#define $b0 $b1 ",
                bBlocks = arrayListOf(
                    arg_k.toBlock(),
                    arg_v.toBlock(),
                ),
            ),
        )

        fun getArgAddAddBlock(
            arg: String,
        ) = getBlock(
            template = "$b0++",
            bBlocks = arrayListOf(
                arg.toBlock(),
            ),
        )

        fun getReturnBlock(
            arg: Block,
        ) = getLineBlock(
            getBlock(
                template = "return $b0;",
                bBlocks = arrayListOf(
                    arg,
                ),
            )
        )

        fun getFunDefineBlock(
            return_type: String,
            method_name: String,
            args_block: Block,
            bBlocks: ArrayList<Block> = arrayListOf(),
        ) = getBlock(
            template = "\n$b0 $b1($b2) {" +
                    "$w0$b3" +
                    "\n}",
            bBlocks = arrayListOf(
                return_type.toBlock(),
                method_name.toBlock(),
                args_block,
                getEmptyBlock(bBlocks),
            ),
        )

        fun getMainFunDefineBlock(
            bBlocks: ArrayList<Block> = ArrayList(),
        ) = getFunDefineBlock(
            return_type = "int",
            method_name = "main",
            args_block = getEmptyBlock(),
            bBlocks = bBlocks,
        )

        fun getForBlock1(
            block0: Block,
            block1: Block,
            block2: Block,
            bBlocks: ArrayList<Block> = ArrayList(),
        ) = getBlock(
            template = "\nfor($b0; $b1; $b2){" +
                    "$w0$b3" +
                    "\n}",
            arrayListOf(
                block0,
                block1,
                block2,
                getEmptyBlock(bBlocks),
            ),
        )

        fun getForBlock11(
            arg_i: String,
            i_min: String,
            i_max: String,
            bBlocks: ArrayList<Block> = ArrayList(),
        ) = getForBlock1(
            getArgAssignBlock(arg_i.toBlock(), i_min.toBlock()),
            getBoolLeBlock(arg_i.toBlock(), i_max.toBlock()),
            getArgAddAddBlock(arg_i),
            bBlocks,
        )

        fun getForBlock11(
            arg_i: String,
            i_min: Int,
            i_max: Int,
            bBlocks: ArrayList<Block> = ArrayList(),
        ) = getForBlock11(
            arg_i,
            i_min.toString(),
            i_max.toString(),
            bBlocks,
        )

        fun getForBlock11(
            arg_i: String,
            i_min: Int,
            i_max: String,
            bBlocks: ArrayList<Block> = ArrayList(),
        ) = getForBlock11(
            arg_i,
            i_min.toString(),
            i_max,
            bBlocks,
        )

        companion object {
            val bf = CBlockFactory()
        }

    }
}