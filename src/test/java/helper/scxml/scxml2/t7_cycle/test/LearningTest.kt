package helper.scxml.scxml2.t7_cycle.test

import helper.base.BaseTypeHelper.ListExpand.toArrayList
import helper.base.DebugHelper.getDebuggerList
import helper.base.LHMHelper.A3LHM
import helper.base.LHMHelper.LHMExpand.add
import helper.scxml.scxml2.EnvHelper
import helper.scxml.scxml2.EnvHelper.RunResult
import helper.scxml.scxml2.MathHelper
import helper.scxml.scxml2.MathHelper.Expand.getEuclideanDistance
import helper.scxml.scxml2.StrategyTripleHelper
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toClockValuations
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toLocationEventVListLHM
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toMeanCost
import helper.scxml.scxml2.t7_cycle.EnvObjHelper
import helper.scxml.scxml2.t7_cycle.LearningHelper
import org.junit.Test

internal class LearningTest {
    @Test
    fun t1() {
        val debuggerList = getDebuggerList(
            0,
            0,
        )
        val hAU = LearningHelper.HyperArgUnit.getObj1()
        val iAU = LearningHelper.InstanceArgUnit.getObj1()
        var heap = ArrayList<RunResult>()
        val env = EnvObjHelper.getEnvObj1()
        val meanList = ArrayList<Double>()
        repeat(
            hAU.maxIterations
        ) iteration@{
            val rrs = ArrayList<RunResult>()
            debuggerList.pln("iterations_${it}:\n")
            repeat(hAU.maxRuns) {
                env.reset()
                env.taskRun2(
                    debuggerList = debuggerList,
                ).let {
                    rrs.add(it)
                }
            }
            val mean1 = rrs.toMeanCost()
            val sorted: List<RunResult> = rrs.sortedBy {
                it.endData["c"]!!.toInt()
            }.take(hAU.maxGood)
            sorted.map {
                heap.add(it)
            }
            heap = heap.sortedBy {
                it.endData["c"]!!.toInt()
            }.take(hAU.maxBest).toArrayList()

            val locationEventVListLHM = heap.toLocationEventVListLHM()
            val locationEventVMeanLHM = A3LHM<String, String, MathHelper.ClockValuations>()
            locationEventVListLHM.touch { a1, a2, a3 ->
                locationEventVMeanLHM.add(a1, a2, a3.mean)
            }
            val oldGetIRenEventSelectorFun = env.strategyTuple.getRenEventSelectorFun
            env.strategyTuple.getRenEventSelectorFun = { scxmlTuple ->
                object : StrategyTripleHelper.IRenEventSelector {
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
                                                0 until 100
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
                                        return helper.base.MathHelper.getRandomString(lhm)
                                    }
                                }
                            }
                        }
                        return null
                    }
                }
            }

            val evaluateResultList = ArrayList<RunResult>()

            repeat(hAU.evalRuns) {
                env.reset()
                env.taskRun2(
                    debuggerList = debuggerList,
                ).let {
                    evaluateResultList.add(it)
                }
            }
            val mean2 = evaluateResultList.toMeanCost()
            meanList.add(mean1)
            meanList.add(mean2)
            debuggerList.pln("mean3=${mean1.coerceAtMost(mean2)}")
            if (mean1 < mean2) {
                env.strategyTuple.getRenEventSelectorFun = oldGetIRenEventSelectorFun
                iAU.nowCountOfNoBetter += 1
                if (iAU.nowCountOfNoBetter >= hAU.maxNoBetter) {
                    //重置
                    env.strategyTuple.getRenEventSelectorFun = EnvObjHelper.getRenEventSelectorFunObj1()
                    iAU.nowCountOfNoBetter = 0
                    iAU.nowCountOfReset += 1
                    if (iAU.nowCountOfReset >= hAU.maxResets) {
                        debuggerList.pln("nowCountOfReset>=maxResets\t\t${iAU.nowCountOfReset}>=${hAU.maxResets}")
                        debuggerList.pln("minMean=${meanList.minOrNull()}")
                        return
                    }
                }
            }
        }
        debuggerList.pln("nowCountOfReset=${iAU.nowCountOfReset}")
        debuggerList.pln("minMean=${meanList.minOrNull()}")
    }
}