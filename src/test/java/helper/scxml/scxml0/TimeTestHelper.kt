package helper.scxml.scxml0

import helper.scxml.scxml2.Res
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.strategy.ScxmlOneStrategyHelper.SNode

object TimeTestHelper {
    fun getSCXMLTuple(): SCXMLTuple {
        return Scxml2Helper.getSCXMLTuple("scxml2/t_time/time.scxml")
    }

    fun getSNode1() = SNode.getRootNode().also {
        it.addMiddleNode1(
            hashSetOf(
                "reset",
            ),
            "10 <= ${Res.globalTimeId} <= 10",
            "start" to 1.0
        )
        it.addMiddleNode1(
            hashSetOf(
                "running",
            ),
            "20 <= ${Res.globalTimeId} <= 20",
            "stop" to 1.0
        )
        it.addMiddleNode1(
            hashSetOf(
                "stopped",
            ),
            "30 <= ${Res.globalTimeId} <= 30",
            "start" to 1.0
        )
        it.addMiddleNode1(
            hashSetOf(
                "running",
            ),
            "40 <= ${Res.globalTimeId} <= 40",
            "reset" to 1.0
        )
        it.addMiddleNode1(
            hashSetOf(
                "reset",
            ),
            "50 <= ${Res.globalTimeId} <= 50",
            "start" to 1.0
        )
    }
}