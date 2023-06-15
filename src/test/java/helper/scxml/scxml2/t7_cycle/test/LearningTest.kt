package helper.scxml.scxml2.t7_cycle.test

import helper.ChartHelper
import helper.base.BaseTypeHelper.LHMExpand.StringDoubleExpand.getMaxKey
import helper.base.BaseTypeHelper.ListExpand.toArrayList
import helper.base.DebugHelper.getDebuggerList
import helper.base.LHMHelper.A3LHM
import helper.base.LHMHelper.LHMExpand.add
import helper.base.MathHelper
import helper.base.TimeHelper
import helper.scxml.scxml2.EnvHelper
import helper.scxml.scxml2.EnvHelper.RunResult
import helper.scxml.scxml2.MathHelper.ClockValuations
import helper.scxml.scxml2.MathHelper.Expand.getEuclideanDistance
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toClockValuations
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toLocationEventVListLHM
import helper.scxml.scxml2.t7_cycle.EnvObjHelper
import helper.scxml.scxml2.t7_cycle.LearningHelper
import org.junit.Test
import res.FileRes

internal class LearningTest {
    @Test
    fun t1() {
        val nowTimeStr = TimeHelper.now(TimeHelper.TimePattern.p4)
        val debuggerList = getDebuggerList(
            0,
            0,
            1,
        )
        val ifDeterminingDetermining = true
        val hAU = LearningHelper.HyperArgUnit.getObj1()
        val iAU = LearningHelper.InstanceArgUnit.getObj1()
        val env = EnvObjHelper.getEnvObj1()
        repeat(
            hAU.maxIterations
        ) iteration@{
            debuggerList.pln("iterations_${it}:\n")
            val rrs = ArrayList<RunResult>()
            env.repeatRun2AndRecord(
                times = hAU.maxRuns,
                lhm = iAU.renEventSelectorCostListLHM,
                runResultList = rrs,
                debuggerList = debuggerList,
            )
            val mean1 = iAU.renEventSelectorCostListLHM[env.strategyTuple.getRenEventSelectorFun]!!.average()
            iAU.meanList.add(mean1)
            val sorted: List<RunResult> = rrs.sortedBy {
                it.endData["c"]!!.toInt()
            }.take(hAU.maxGood)
            sorted.map {
                iAU.heap.add(it)
            }
            iAU.heap = iAU.heap.sortedBy {
                it.endData["c"]!!.toInt()
            }.take(hAU.maxBest).toArrayList()

            val locationEventVListLHM = iAU.heap.toLocationEventVListLHM()
            val locationEventVMeanLHM = A3LHM<String, String, ClockValuations>()
            locationEventVListLHM.touch { a1, a2, a3 ->
                locationEventVMeanLHM.add(a1, a2, a3.mean)
            }
            val oldGetIRenEventSelectorFun = env.strategyTuple.getRenEventSelectorFun
            env.strategyTuple.getRenEventSelectorFun = { scxmlTuple ->
                object : IRenEventSelector {
                    val dataXInt = scxmlTuple.dataSCXML.getDataInt("x")!!
                    override fun getEvent(stateId: String): String? {
                        when (stateId) {
                            "s1" -> {
                                when (dataXInt) {
                                    100 -> {
                                        return "s1s4t1"
                                    }
                                    else -> {
                                        //为了均匀
                                        if (!EnvHelper.T3BaseEnv.ifCanNextWhenOneClock(
                                                dataXInt,
                                                0 until 100,
                                            )
                                        ) return null
                                        val lhm = LinkedHashMap<String, Double>()
                                        locationEventVMeanLHM[stateId]!!.map { (event, mean) ->
                                            lhm.add(
                                                event,
                                                mean.getEuclideanDistance(
                                                    scxmlTuple.toData().toClockValuations(),
                                                ),
                                            )
                                        }
                                        return if (ifDeterminingDetermining) {
                                            lhm.getMaxKey()
                                        } else {
                                            MathHelper.getRandomString(lhm)
                                        }
                                    }
                                }
                            }
                        }
                        return null
                    }
                }
            }

            env.repeatRun2AndRecord(
                times = hAU.evalRuns,
                lhm = iAU.renEventSelectorCostListLHM,
                debuggerList = debuggerList,
            )

            val mean2 = iAU.renEventSelectorCostListLHM[env.strategyTuple.getRenEventSelectorFun]!!.average()
            if (mean2 < iAU.lastMinCost) {
                iAU.nowCountOfNoBetter = 0
                iAU.lastMinCost = mean2
            } else {
                env.strategyTuple.getRenEventSelectorFun = oldGetIRenEventSelectorFun
                iAU.nowCountOfNoBetter += 1
                if (iAU.nowCountOfNoBetter >= hAU.maxNoBetter) {
                    //重置
                    iAU.renEventSelectorCostListLHM = LinkedHashMap()
                    iAU.renEventSelectorCostListLHMList.add(iAU.renEventSelectorCostListLHM)
                    iAU.resetLastMinCost()
                    env.strategyTuple.getRenEventSelectorFun = EnvObjHelper.getRenEventSelectorFunObj1()
                    iAU.nowCountOfNoBetter = 0
                    iAU.nowCountOfReset += 1
                    if (iAU.nowCountOfReset >= hAU.maxResets) {
                        debuggerList.pln(
                            "nowCountOfReset>=maxResets\t\t${iAU.nowCountOfReset}>=${hAU.maxResets}",
                            arrayListOf(0, 1, 2),
                        )
                        debuggerList.pln(
                            "minMean=${iAU.meanList.minOrNull()}",
                            arrayListOf(0, 1, 2),
                        )
                        return@iteration
                    }
                }
            }
        }
        debuggerList.pln(
            "nowCountOfReset=${iAU.nowCountOfReset}",
            arrayListOf(0, 1, 2),
        )

        iAU.renEventSelectorCostListLHMList.withIndex()
            .map { (k: Int, v: LinkedHashMap<(SCXMLTuple) -> IRenEventSelector, ArrayList<Double>>) ->
                println("k=${k}")
                val averages = v.map { (_: (SCXMLTuple) -> IRenEventSelector, v: ArrayList<Double>) ->
                    v.average()
                }
                averages.map {
                    //打印均值
                    debuggerList.pln(
                        it.toString(),
                        arrayListOf(0, 1, 2),
                    )
                }
                ChartHelper.taskDrawLineChart(
                    averages.toArrayList(),
                    "${FileRes.out_chart_file}" +
                            "/LearningTest_t1" +
                            "/t_${nowTimeStr}" +
                            "/chart${k}.png"
                )
            }
    }
}