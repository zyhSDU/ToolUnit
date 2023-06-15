package helper.scxml.scxml2.t4_time

import helper.base.DebugHelper.DebuggerList.Companion.getDebuggerList
import helper.base.MathHelper
import helper.scxml.scxml0.TimeTestHelper
import helper.scxml.scxml2.Expand.SCXMLExecutorExpand.isInState
import helper.scxml.scxml2.Res
import helper.scxml.scxml2.SCXMLTuple
import org.apache.commons.scxml2.model.TransitionTarget
import org.junit.Test

internal class TimeTest2 {
    class SUnit(
        val stateId: String,
        val dataExpr: Int,
        val intRange: IntRange,
        val stringDoubleLHM: LinkedHashMap<String, Double>,
    ) {
        constructor(
            stateId: String,
            dataExpr: Int,
            intRange: Int,
            stringDoubleLHM: LinkedHashMap<String, Double>,
        ) : this(
            stateId,
            dataExpr,
            intRange..intRange,
            stringDoubleLHM,
        )

        fun tryFire(
            scxmlTuple: SCXMLTuple,
            doOnEntry: (TransitionTarget) -> Unit = {},
            fireNext: () -> Unit,
        ): String? {
            var event: String? = null
            val executor = scxmlTuple.executor
            if (executor.isInState(stateId)) {
                if (dataExpr in intRange) {
                    MathHelper.getRandomStringWithLeftTime(
                        stringDoubleLHM,
                        intRange.last - dataExpr + 1,
                    )?.let {
                        scxmlTuple.fireEvent(it, doOnEntry)
                        event = it
                        fireNext()
                    }
                }
            }
            return event
        }
    }

    @Test
    fun testRun() {
        val globalTimeId = Res.globalTimeId
        val scxmlTuple = TimeTestHelper.getSCXMLTuple()
        val executor = scxmlTuple.executor
        val mySCXML = scxmlTuple.dataSCXML

        fun strategyFireEvent() {
            val dataT: Int = mySCXML.getDataInt(globalTimeId)!!
            var ifFired = false
            arrayListOf(
                SUnit("reset", dataT, 10, linkedMapOf("start" to 1.0)),
                SUnit("running", dataT, 20, linkedMapOf("stop" to 1.0)),
                SUnit("stopped", dataT, 30, linkedMapOf("start" to 1.0)),
                SUnit("running", dataT, 40, linkedMapOf("reset" to 1.0)),
                SUnit("reset", dataT, 50, linkedMapOf("start" to 1.0)),
            ).map {
                if (!ifFired) {
                    ifFired = it.tryFire(scxmlTuple, {}, ::strategyFireEvent) != null
                }
            }
        }

        executor.go()

        scxmlTuple.statusPrintln()
        while (true) {
            strategyFireEvent()
            if (mySCXML.ifDataExprEqualInt(globalTimeId, 60)) {
                break
            }
            scxmlTuple.fireEvent(
                event = "time",
                debuggerList = getDebuggerList(0),
            )
        }
        scxmlTuple.statusPrintln()
    }
}