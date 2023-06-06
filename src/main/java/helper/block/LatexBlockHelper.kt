package helper.block

import helper.block.BlockHelper.Block
import helper.block.BlockHelper.BlockFactory
import helper.block.BlockHelper.Expand.BlockListTo.joinToBlock
import helper.block.BlockHelper.Expand.BlockTo.toLineBlock
import helper.block.BlockHelper.Expand.ToBlock.toBlock
import helper.block.BlockHelper.Expand.ToBlock.toLineBlock
import helper.block.BlockHelper.Language
import helper.block.BlockHelper.b0
import helper.block.BlockHelper.b1
import helper.block.BlockHelper.language_latex
import helper.block.LatexBlockHelper.LatexBlockFactory.ToLabelBlock.toAuthorLabelBlock
import helper.block.LatexBlockHelper.LatexBlockFactory.ToLabelBlock.toDateLabelBlock
import helper.block.LatexBlockHelper.LatexBlockFactory.ToLabelBlock.toDocumentClassLabelBlock
import helper.block.LatexBlockHelper.LatexBlockFactory.ToLabelBlock.toPackageLabelListBlock
import helper.block.LatexBlockHelper.LatexBlockFactory.ToLabelBlock.toTitleLabelBlock

object LatexBlockHelper {

    open class LatexBlockFactory(
        language: Language = language_latex,
    ) : BlockFactory(
        language = language,
    ) {
        fun getA0LabelBlock(
            labelName: String,
            labelArg: String,
        ): Block {
            return bf.getBlock("\n\\$b0{$b1}", labelName, labelArg)
        }

        object ToLabelBlock {
            fun String.toDocumentClassLabelBlock() = bf.getA0LabelBlock("documentclass", this)
            fun String.toPackageLabelBlock() = bf.getA0LabelBlock("usepackage", this)
            fun String.toTitleLabelBlock() = bf.getA0LabelBlock("title", this)
            fun String.toAuthorLabelBlock() = bf.getA0LabelBlock("author", this)
            fun String.toDateLabelBlock() = bf.getA0LabelBlock("date", this)

            fun ArrayList<String>.toPackageLabelListBlock() =
                this.map { it.toPackageLabelBlock() }.joinToBlock().toLineBlock()
        }

        fun getA1LabelBlock(
            labelName: String,
            labelArg: String,
            block: Block,
        ): Block {
            return bf.getEmptyBlock(
                "".toLineBlock(),
                bf.getBlock(
                    "\\$b0{$b1}",
                    labelName.toBlock(),
                    labelArg.toBlock(),
                ),
                "".toLineBlock(),
                block,
            )
        }

        fun getSectionBlock(
            sectionName: String,
            block: Block,
        ): Block {
            return getA1LabelBlock(
                "section",
                sectionName,
                block,
            )
        }

        fun getA2LabelBlock(
            labelName: String,
            block: Block,
        ): Block {
            return bf.getBlock(
                "\n\\begin{$b0}" +
                        b1 +
                        "\n\\end{$b0}",
                labelName.toBlock(),
                block,
            )
        }

        fun getDocumentBlockBlock(
            vararg block: Block,
        ): Block {
            return bf.getA2LabelBlock(
                "document",
                bf.getEmptyBlock(
                    "\\maketitle".toLineBlock(),
                    *block,
                )
            )
        }

        fun getTexBlock(
            packages: ArrayList<String>,
            title: String,
            author: String,
            date: String,
            documentContentBlock: Block,
        ): Block {
            return bf.getEmptyBlock(
                "article".toDocumentClassLabelBlock(),
                packages.toPackageLabelListBlock(),
                title.toTitleLabelBlock(),
                author.toAuthorLabelBlock(),
                date.toDateLabelBlock(),
                documentContentBlock,
            )
        }

        fun getNewLineBlock(): Block {
            return "\\\\".toBlock().toLineBlock()
        }

        fun getEmptySetBlock(): Block {
            return "\\emptyset".toBlock()
        }

        fun getNonNegativeRealSetBlock() {

        }

        companion object {
            val bf = LatexBlockFactory()

            fun Block.toMathFormulaBlock(): Block {
                return bf.get010Block(
                    "\$".toBlock(),
                    this,
                )
            }

            fun String.toMathFormulaBlock(): Block {
                return this.toBlock().toMathFormulaBlock()
            }

            fun Block.toSubScriptBlock(): Block {
                return bf.getBlock(
                    template = "_{$b0}",
                    bBlocks = arrayListOf(this),
                )
            }

            fun Block.toSuperscriptBlock(): Block {
                return bf.getBlock(
                    template = "^{$b0}",
                    bBlocks = arrayListOf(this),
                )
            }

            fun Block.toSuperscriptBlock2(): Block {
                return bf.getBlock(
                    template = "^{($b0)}",
                    bBlocks = arrayListOf(this),
                )
            }

            fun Block.toUnderSetBlock(
                underBlock: Block,
            ): Block {
                return bf.getBlock(
                    //\usepackage{amsmath}
                    template = "\\stackrel{$b0}{$b1}",
                    arrayListOf(
                        this,
                        underBlock,
                    ),
                )
            }

            fun Block.toUnderSetArrowBlock(): Block {
                return this.toUnderSetBlock("\\longrightarrow".toBlock())
            }
        }
    }
}