package helper.scxml.scxml2.t4_time

import helper.base.ConstraintHelper.CompareOperator.Companion.eOp
import helper.base.ConstraintHelper.CompositeConstraint.Expand.toCompositeConstraint
import helper.base.ConstraintHelper.N1Constraint
import helper.base.DebugHelper.Debugger.Companion.getDebuggerByInt
import helper.scxml.scxml0.TimeTestHelper
import helper.scxml.scxml2.Res
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.Scxml2Helper
import org.junit.Test

internal class TimeTest {
    private fun getSCXMLTuple(): SCXMLTuple {
        return Scxml2Helper.getSCXMLTuple("scxml2/t_time/time.scxml")
    }

    //废弃
    @Test
    fun testCalculateCost() {
        val scxmlTuple = getSCXMLTuple()
        val strategyNode = TimeTestHelper.getSNode1()
        scxmlTuple.calculateCost(
            strategyNode,
            Scxml2Helper.MyDataList().also {
                scxmlTuple.dataSCXML.scxml.datamodel.data.map { data ->
                    it.dataLHM[data.id] = data
                }
            },
            listOf(),
            N1Constraint(
                arrayListOf(Res.globalTimeId),
                eOp,
                60.0,
            ).toCompositeConstraint(),
            debugger = getDebuggerByInt(1, 0, 11),
        )
    }

    //使用规范策略结构
    @Test
    fun testRun() {
        val scxmlTuple = getSCXMLTuple()
        val executor = scxmlTuple.executor
        val strategyNode = TimeTestHelper.getSNode1()

        executor.go()

        scxmlTuple.statusPrintln()
        while (true) {
            scxmlTuple.tryFire(strategyNode)
            if (
                scxmlTuple.dataSCXML.ifDataExprEqualInt(
                    Res.globalTimeId,
                    60,
                )
            ) {
                break
            }
            scxmlTuple.fireEvent("time")
        }
    }
}