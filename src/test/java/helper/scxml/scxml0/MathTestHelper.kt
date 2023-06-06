package helper.scxml.scxml0

import helper.scxml.strategy.ScxmlOneStrategyHelper.SNode

object MathTestHelper {
    fun getSNode1() = SNode.getRootNode().also {
        it.addMiddleNode1(
            hashSetOf(
                "f_1_m_1_a1",
            ),
            "0 <= T <= 2",
            "bike" to 1.0
        )
    }
}