package helper.scxml.scxml1.tests3

import helper.base.FileHelper
import helper.base.LHMHelper
import helper.base.MathHelper
import helper.base.PrintHelper.StringTo.toPrintln
import helper.scxml.scxml1.Scxml1Helper
import helper.scxml.scxml1.Scxml1Helper.DataExpand.exprAddOne
import helper.scxml.scxml1.Scxml1Helper.DataExpand.exprEqualsInt
import helper.scxml.strategy.ScxmlOneStrategyHelper.SNode
import helper.scxml.strategy.ScxmlOneStrategyHelper.StrategyNodeKeyTypeVarConstraint
import helper.scxml.strategy.ScxmlOneStrategyHelper.getStrategyLeafNode
import helper.scxml.ScxmlVarHelper.ClockConstraint.ToClockConstraint.toClockConstraint
import helper.scxml.scxml0.TrafficTestHelper
import helper.scxml.scxml0.TrafficTestHelper.SIST
import helper.scxml.scxml0.TrafficTestHelper.add
import org.apache.commons.scxml.model.TransitionTarget
import org.junit.Test
import java.io.File
import kotlin.math.max
import kotlin.math.min

internal class TrafficTest {
    class T4StateMachine : Scxml1Helper.StateMachine(
        "scxml/tests3/t4_traffic.scxml",
    ) {
        fun Aalborg() {}
        fun Bike() {}
        fun Car() {}
        fun Easy() {}
        fun Heavy() {}
        fun Train() {}
        fun Go() {}
        fun Wait() {}
        fun GoBack() {}
        fun Sydney() {}
    }

    fun getEnvStrategyNode1(): SNode {
        return SNode.getRootNode().also {
            it.addMiddleNode1("Aalborg", "0 <= T <= 2", "bike" to 1.0)
            it.addMiddleNode1("Bike", "42 <= T <= 45", "bike_end" to 1.0)
        }
    }

    @Test
    fun testRunT4StateMachineTest1() {
        fun runT4StateMachine(
            envStrategyNode: SNode,
        ) {
            val t4StateMachine = T4StateMachine()
            t4StateMachine.printlnLatexBlockString()
            t4StateMachine.printlnStructBlockString()
            t4StateMachine.printlnCurrentConfigure()

            while (true) {
                t4StateMachine.tryFire(envStrategyNode)
                if (t4StateMachine.globalTime.exprEqualsInt(60)) {
                    break
                }
                if (t4StateMachine.isOnState("Sydney")) {
                    break
                } else {
                    t4StateMachine.setDataExprAddOne("T")
                    t4StateMachine.globalTime.exprAddOne()
                    t4StateMachine.printlnCurrentConfigure()
                }
            }
        }
        runT4StateMachine(getEnvStrategyNode1())
    }

    @Test
    fun SNodeGetTraceTest() {
        val envStrategyNode1 = getEnvStrategyNode1()
        envStrategyNode1.toBlock().getStr().toPrintln()
        envStrategyNode1.children!![0].children!![0].children!![0].getTrace().let {
            it.map {
                it.toString().toPrintln()
            }
        }
    }

    @Test
    fun eventUnitLHMTest() {
        val t4StateMachine = T4StateMachine()
        t4StateMachine.eventUnitLHM.map { (k, v) ->
            k.toPrintln()
            v.map { (k, v) ->
                "\t${k}".toPrintln()
                v.map {
                    "\t\t${v}".toPrintln()
                }
            }
        }
    }

    /**
     * 测试
     * 给定策略下计算期望
     */
    fun calculateExpectationTest(envStrategyNode: SNode) {
        T4StateMachine()
            .calculateExpectation(envStrategyNode, 60)
            .toString().toPrintln()
    }

    @Test
    fun calculateExpectationTest1() {
        calculateExpectationTest(TrafficTestHelper.getSNode1())
        //输出44.5
    }

    @Test
    fun calculateExpectationTest2() {
        calculateExpectationTest(TrafficTestHelper.getSNode2())
    }

    @Test
    fun calculateExpectationTest3() {
        calculateExpectationTest(TrafficTestHelper.getSNode3())
    }

    //discard
    fun Scxml1Helper.StateMachine.getTrace(
        envStrategyNode: SNode,
        globalTimeMax: Int,
        lhm: LHMHelper.A4LHM<String, Int, String, SIST>,
        sb: StringBuilder = StringBuilder(),
    ): String {
        val sists = ArrayList<SIST>()
        this.resetMachine()
        var nowTarget = this.engine.stateMachine.initialTarget
        sb.append("${getCurrentConfigure().toFormalBlock().getStr()}\n")

        while (true) {
            getStrategyLeafNode(
                envStrategyNode = envStrategyNode,
                IDataExpand = this,
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
                                val nowTime = this.getData(it.varId)!!.expr.toInt()
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
                    lhm.add(nowTarget.id, this.globalTime.expr.toInt(), event)
                    sists.add(lhm[nowTarget.id]!![this.globalTime.expr.toInt()]!![event]!!)
                }
                sb.append("${getCurrentConfigure().toFormalBlock().getStr()}\n")
                sb.append("fireEvent:${event}\n")
                this.fireEvent(event)
//                nowTarget.onExit.doAssign(this)
                //1个event可能对应多个transition，暂时先取第0个，1个transition可能对应多个target，暂时取第0个
                nowTarget = this.eventUnitLHM[event]!![nowTarget.id]!![0].transition.targets[0] as TransitionTarget
                sb.append("${getCurrentConfigure().toFormalBlock().getStr()}\n")
            }
            if (globalTime.exprEqualsInt(globalTimeMax)) {
                sists.map {
                    it.unSafeCount += 1
                }
                break
            }
            if (isOnState("Sydney")) {
                sists.map {
                    it.safeResults.add(globalTime.expr.toInt())
                }
                break
            }
            setDataExprAddOne("T")
            globalTime.exprAddOne()
        }
        return sb.toString()
    }

    //discard
    @Test
    fun randomTrace() {
        val pathIndex = 1
        val ifPrintEveryOne = false
        //
        val path = "out/log/scxml1/t4trace${pathIndex}"
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
        repeat(10) {
            println("repeat${it}:")
            T4StateMachine().getTrace(
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