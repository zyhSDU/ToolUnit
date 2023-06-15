package helper.scxml.scxml2.t7_cycle.test

import helper.ChartHelper
import helper.base.BaseTypeHelper.LHMExpand.StringDoubleExpand.getMaxKey
import helper.base.BaseTypeHelper.LHMExpand.StringDoubleExpand.getMinKey
import helper.base.BaseTypeHelper.ListExpand.getMinValueKey
import helper.base.BaseTypeHelper.ListExpand.toArrayList
import helper.base.DebugHelper.Debugger.Companion.getDebuggerByInt
import helper.base.DebugHelper.DebuggerList.Companion.getDebuggerList
import helper.base.LHMHelper.A3LHM
import helper.base.LHMHelper.LHMExpand.add
import helper.base.LHMHelper.LHMExpand.toStr
import helper.base.MathHelper
import helper.base.TimeHelper
import helper.scxml.scxml2.EnvHelper
import helper.scxml.scxml2.EnvHelper.RunResult
import helper.scxml.scxml2.MathHelper.ClockValuations
import helper.scxml.scxml2.MathHelper.Expand.getEuclideanDistance
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toClockValuations
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toCostList
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toLocationEventVListLHM
import helper.scxml.scxml2.t7_cycle.EnvObjHelper
import helper.scxml.scxml2.t7_cycle.LearningHelper
import org.junit.Test
import res.FileRes

internal class LearningTest {
    fun t1() {
        val debugger_renEventSelectorCostListLHM = getDebuggerByInt()
        val nowTimeStr = TimeHelper.now(TimeHelper.TimePattern.p4)
        val debuggerList = getDebuggerList(
            0,
            0,
            1,
        )
//        debuggerList.arr.add(RunResult.debugger)
//        debuggerList.arr.add(debugger_renEventSelectorCostListLHM)
        val ifDeterminingDetermining = true
        val hAU = LearningHelper.HyperArgUnit.getObj1()
        val iAU = LearningHelper.InstanceArgUnit.getObj1()
        val env = EnvObjHelper.getEnvObj1()
        repeat(
            hAU.maxIterations
        ) iteration@{
            if (iAU.nowCountOfReset >= hAU.maxResets) return@iteration
            debuggerList.pln("iterations_${it}:\n")
            println("indexAfterReset=${iAU.indexAfterReset}")
            iAU.indexAfterReset += 1
            val rrs = ArrayList<RunResult>()

            env.repeatRun2AndRecord(
                times = hAU.maxRuns,
                lhm = iAU.renEventSelectorCostListLHM,
                runResultList = rrs,
                debuggerList = debuggerList,
            )
            debuggerList.pln2(
                "after run\n" +
                        "${iAU.renEventSelectorCostListLHM.toStr()}\n",
                arrayListOf(
                    debugger_renEventSelectorCostListLHM,
                ),
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

            debuggerList.pln2(
                "after eval\n" +
                        "${iAU.renEventSelectorCostListLHM.toStr()}\n",
                arrayListOf(
                    debugger_renEventSelectorCostListLHM,
                ),
            )

            val mean2 = iAU.renEventSelectorCostListLHM[env.strategyTuple.getRenEventSelectorFun]!!.average()
            iAU.renEventSelectorCostMean2LHM[env.strategyTuple.getRenEventSelectorFun] = mean2

            println("before compare")
            println("\tmean2=${mean2}")
            println("\tiAU.lastMinCost=${iAU.lastMinCost}")

            if (mean2 < iAU.lastMinCost) {
                iAU.nowCountOfNoBetter = 0
                iAU.lastMinCost = mean2
            } else {
                env.strategyTuple.getRenEventSelectorFun = oldGetIRenEventSelectorFun
                iAU.nowCountOfNoBetter += 1
                if (iAU.nowCountOfNoBetter >= hAU.maxNoBetter) {
                    //重置
                    println("重置")
                    iAU.indexAfterReset = 0
                    iAU.renEventSelectorCostListLHM = LinkedHashMap()
                    iAU.renEventSelectorCostMean2LHM = LinkedHashMap()
                    iAU.renEventSelectorCostListLHMList.add(iAU.renEventSelectorCostListLHM)
                    iAU.renEventSelectorCostMean2LHMList.add(iAU.renEventSelectorCostMean2LHM)
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

        iAU.renEventSelectorCostMean2LHMList.withIndex().map { (index: Int, lhm) ->
            if (lhm.size <= 0) return@map
            println("index=${index}")
            val averages = lhm.map { (_, v) ->
                v
            }
            averages.map {
                //打印均值
                debuggerList.pln(
                    it.toString(),
                    arrayListOf(0, 1, 2),
                )
            }
            ChartHelper.taskDrawLineChart(
                yData = averages.toArrayList(),
                saveFile = "${FileRes.out_chart_file}" +
                        "/LearningTest_t1" +
                        "/t_${nowTimeStr}" +
                        "/chart${index}.png"
            )
        }
        val renEventSelectorCostMean2MinList = ArrayList<Double>()
        var if_break_renEventSelectorCostMean2LHMList = false
        iAU.renEventSelectorCostMean2LHMList.map {
            if (if_break_renEventSelectorCostMean2LHMList) return@map
            if (it.size <= 0) {
                if_break_renEventSelectorCostMean2LHMList = true
                return@map
            }
            val mean2Min = it.values.minOrNull()!!
            renEventSelectorCostMean2MinList.add(mean2Min)
        }
        ChartHelper.taskDrawLineChart(
            yData = renEventSelectorCostMean2MinList,
            ifShowY = false,
            saveFile = "${FileRes.out_chart_file}" +
                    "/LearningTest_t1" +
                    "/t_${nowTimeStr}" +
                    "/chart_min.png"
        )

        assert(renEventSelectorCostMean2MinList.size > 0)
        println("last_test")
        val getMinValueKey = renEventSelectorCostMean2MinList.getMinValueKey()!!
        println("getMinValueKey=${getMinValueKey}")
        println("getMinValue=${renEventSelectorCostMean2MinList[getMinValueKey]}")
        env.strategyTuple.getRenEventSelectorFun = iAU.renEventSelectorCostMean2LHMList[getMinValueKey].getMinKey()!!
        env.repeatRun2AndRecord(
            times = hAU.evalRuns * 10,
            debuggerList = debuggerList,
        ).toCostList().let {
            println("average=${it.average()}")
        }
    }

    @Test
    fun t1t1() {
        t1()
    }
}