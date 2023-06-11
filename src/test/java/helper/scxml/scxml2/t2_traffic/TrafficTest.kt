package helper.scxml.scxml2.t2_traffic

import helper.base.DebugHelper.Debugger
import helper.base.FileHelper
import helper.base.LHMHelper
import helper.base.MathHelper
import helper.base.PrintHelper.StringTo.toPrintln
import helper.scxml.ScxmlVarHelper.ClockConstraint.ToClockConstraint.toClockConstraint
import helper.scxml.scxml2.Expand.DataExpand.ifExprEqualsInt
import helper.scxml.scxml2.Expand.DataExpand.setExprAddOne
import helper.scxml.scxml2.Expand.SCXMLExecutorExpand.isInState
import helper.scxml.scxml2.Expand.SCXMLExpand.getInitialState
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.strategy.ScxmlOneStrategyHelper.SNode
import helper.scxml.strategy.ScxmlOneStrategyHelper.StrategyNodeKeyTypeVarConstraint
import helper.scxml.strategy.ScxmlOneStrategyHelper.getStrategyLeafNode
import helper.scxml.scxml0.TrafficTestHelper
import helper.scxml.scxml0.TrafficTestHelper.SIST
import helper.scxml.scxml0.TrafficTestHelper.add
import org.apache.commons.scxml2.model.TransitionTarget
import org.junit.Test
import java.io.File
import kotlin.math.max
import kotlin.math.min

internal class TrafficTest {
    //出错，globalTime不加一
    //打印太多，不必每秒打印
    @Test
    fun testRun() {
        val scxmlTuple = TrafficTestHelper.getSCXMLTuple()
        val executor = scxmlTuple.executor
        val strategyNode = TrafficTestHelper.getSNode1()

        var globalTime = 0

        fun infoPrintln() {
            scxmlTuple.statusPrintln()
        }

        executor.go()

        infoPrintln()
        while (true) {
            scxmlTuple.tryFire(strategyNode)
            if (globalTime == 60) break
            if (executor.isInState("Sydney")) {
                break
            } else {
                scxmlTuple.dataSCXML.setDataExprAddOne("T")
                globalTime += 1
                infoPrintln()
            }
        }
    }

    @Test
    fun testScxmlInitial() {
        val scxmlTuple = TrafficTestHelper.getSCXMLTuple()
        scxmlTuple.dataSCXML.scxml.initial.toPrintln()
        scxmlTuple.dataSCXML.scxml.children.map {
            it.id.toPrintln()
        }
    }

    fun testCalculateExpectation(envStrategyNode: SNode) {
        TrafficTestHelper
            .getSCXMLTuple()
            .calculateExpectation(
                envStrategyNode,
                60,
            )
            .toString()
            .toPrintln()
    }

    @Test
    fun testCE1() {
        testCalculateExpectation(TrafficTestHelper.getSNode1())
    }

    @Test
    fun testCE2() {
        testCalculateExpectation(TrafficTestHelper.getSNode2())
    }

    @Test
    fun testCE3() {
        testCalculateExpectation(TrafficTestHelper.getSNode3())
    }

    fun SCXMLTuple.getTrace(
        envStrategyNode: SNode,
        globalTimeMax: Int,
        lhm: LHMHelper.A4LHM<String, Int, String, SIST>,
        sb: StringBuilder = StringBuilder(),
    ): String {
        val sists = ArrayList<SIST>()
        this.executor.reset()
        var nowTarget: TransitionTarget = this.dataSCXML.scxml.getInitialState()
        sb.append("${this.getCurrentConfigureStr()}\n")

        while (true) {
            getStrategyLeafNode(
                envStrategyNode = envStrategyNode,
                IDataExpand = this.dataSCXML,
                filterStateFun = { it.size == 1 && it.toList()[0] == nowTarget.id },
            )?.let { sNode ->
                var leftTime = Int.MAX_VALUE
                sNode.getTrace().map {
                    if (it.isMiddleNode()) {
                        val strategyNodeKeyType = it.strategyNodeKeyType
                        if (strategyNodeKeyType!! is StrategyNodeKeyTypeVarConstraint) {
                            strategyNodeKeyType as StrategyNodeKeyTypeVarConstraint
                            strategyNodeKeyType.key.toClockConstraint()!!.let {
                                var minTime = it.minV
                                val maxTime = it.maxV
                                val nowTime = this.dataSCXML.getData(it.varId)!!.expr.toInt()
                                minTime = max(minTime, nowTime)
                                leftTime = min(leftTime, maxTime - minTime + 1)
                            }
                        }
                    }
                }
                //采用平均分布
                val event = MathHelper.getRandomStringWithLeftTime(
                    sNode.eventDPLHM!!,
                    leftTime,
                ) ?: return@let
                if (nowTarget.id == "Aalborg" || nowTarget.id == "Wait") {
                    lhm.add(nowTarget.id, dataSCXML.globalTimeData.expr.toInt(), event)
                    sists.add(lhm[nowTarget.id]!![dataSCXML.globalTimeData.expr.toInt()]!![event]!!)
                }
                sb.append("${this.getCurrentConfigureStr()}\n")
                this.fireEvent(event)
                //1个event可能对应多个transition，暂时先取第0个，1个transition可能对应多个target，暂时取第0个
                nowTarget = this.eventUnitLHM[event]!![nowTarget.id]!![0].transition.targets.toList()[0]
                sb.append("${this.getCurrentConfigureStr()}\n")
            }
            if (dataSCXML.globalTimeData.ifExprEqualsInt(globalTimeMax)) {
                sists.map {
                    it.unSafeCount += 1
                }
                break
            }
            if (this.executor.isInState("Sydney")) {
                sists.map {
                    it.safeResults.add(dataSCXML.globalTimeData.expr.toInt())
                }
                break
            }
            this.dataSCXML.setDataExprAddOne("T")
            dataSCXML.globalTimeData.setExprAddOne()
        }
        return sb.toString()
    }

    @Test
    fun testCE4_randomTrace() {
        val debugger = Debugger(1)

        val pathIndex = 2
        val ifPrintEveryOne = false
        //
        val path = "out/log/scxml2/t4trace${pathIndex}"
        val envStrategyNode = TrafficTestHelper.getSNode4_randomSNode()
        //
        val lhm = LHMHelper.A4LHM<String, Int, String, SIST>()
        arrayOf("Aalborg", "Wait").map { i1 ->
            lhm[i1] = LHMHelper.A3LHM()
            (0..60).map { i2 ->
                lhm[i1]!![i2] = LinkedHashMap()
                arrayOf("car", "bike", "train").map { i3 ->
                    lhm[i1]!![i2]!![i3] = SIST(i1, i2, i3)
                }
            }
        }
        //
        FileHelper.createDirIfNotExists(path)
        //
        repeat(10000) {
            debugger.pln("repeat${it}:")
            TrafficTestHelper.getSCXMLTuple().getTrace(
                envStrategyNode,
                60,
                lhm,
            ).let { str ->
                if (ifPrintEveryOne) {
                    File("${path}/trace_${it}.txt").writeText(str)
                }
            }
        }
        val file = File("${path}/trace.txt")
        //清空
        file.writeText("")
        //从头写
        lhm.touch { a1, a2, a3, a4 ->
            if (!a4.ifEmpty()) {
                file.appendText("${a4.toTextString()}\n")
            }
        }
    }
}
