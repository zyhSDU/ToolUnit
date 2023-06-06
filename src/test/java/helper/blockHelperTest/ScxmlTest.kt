package helper.blockHelperTest

import helper.base.PrintHelper.StringTo.toPrint
import helper.block.BlockHelper.Expand.BlockListTo.joinToBlock
import helper.block.BlockHelper.Expand.BlockTo.toLineBlock
import helper.block.BlockHelper.Expand.ToBlock.toBlock
import helper.block.LatexBlockHelper.LatexBlockFactory.Companion.toMathFormulaBlock
import helper.block.LatexBlockHelper.LatexBlockFactory.Companion.toUnderSetArrowBlock
import helper.block.ScxmlBlockHelper
import helper.block.ScxmlBlockHelper.ScxmlBlockFactory.*
import helper.block.ScxmlBlockHelper.ScxmlBlockFactory.Event.Companion.toEvents
import org.junit.Test

internal class ScxmlTest {
    @Test
    fun testScxmlBlockFactoryTest() {
        val bf = ScxmlBlockHelper.ScxmlBlockFactory.bf
        val s000 = BlockState(
            onEntries = arrayListOf(
                "log:hello-s000",
            ),
        )
        val s001 = BlockState(
            onEntries = arrayListOf(
                "log:hello-s001",
            ),
        )
        s000.transitions.add(
            Transition(
                s001,
                arrayListOf("e1").toEvents(),
            )
        )
        val s00 = BlockState(
            onEntries = arrayListOf(
                "log:hello-s00",
            ),
            initialBlockState = s000,
            finalBlockStates = linkedSetOf(s001),
        )
        val s01 = BlockState(
            onEntries = arrayListOf(
                "log:hello-s01",
            ),
        )
        s00.transitions.add(
            Transition(
                s01,
                arrayListOf("e2").toEvents(),
            )
        )
        val s0 = BlockState(
            initialBlockState = s00,
            finalBlockStates = linkedSetOf(s01),
            dataModel = DataModel(
                arrayListOf(
                    Data("t1", "0"),
                    Data("t2", "0"),
                ),
            )
        )
        val e0 = "e0"
        val e1 = "e1"
        val c0 = Configuration(
            blockStates = arrayListOf(
                s0
            ),
        )
        val c1 = Configuration(
            blockStates = arrayListOf(
                s0, s00
            ),
        )
        val c2 = Configuration(
            blockStates = arrayListOf(
                s0, s00, s000
            ),
        )
        val c3 = Configuration(
            blockStates = arrayListOf(
                s0, s00, s001
            ),
        )
        val c4 = Configuration(
            blockStates = arrayListOf(
                s0, s00,
            ),
        )
        val c5 = Configuration(
            blockStates = arrayListOf(
                s0, s01,
            ),
        )
        val c6 = Configuration(
            blockStates = arrayListOf(
                s0,
            ),
        )
        s0.updateId(arrayListOf(0))
        val cs = arrayListOf(c0, c1, c2, c3, c4, c5, c6)
        val es = arrayListOf("enter:s00", "enter:s000", e0, "exit:s001", e1, "exit:s01").toEvents()
        val block = bf.getEmptyBlock(
            ".".toBlock(),
            bf.getNewLineBlock(),
            s0.toLatexBlock(),
            bf.getNewLineBlock(),
            bf.getEmptyBlock(
                es.indices.map {
                    bf.getEmptyBlock(
                        cs[it].toSelfBlock(),
                        es[it].toLatexBlock().toUnderSetArrowBlock(),
                    )
                }.joinToBlock(""),
                cs[cs.size - 1].toSelfBlock(),
            ).toMathFormulaBlock().toLineBlock(),
        )
        val res = block.getStr()
        res.toPrint()
    }
}