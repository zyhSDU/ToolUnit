package helper.scxml.scxml2.t7_cycle

import helper.base.BaseTypeHelper.ListExpand.toArrayList
import helper.base.DebugHelper.getDebuggerList
import helper.base.LHMHelper.A3LHM
import helper.base.LHMHelper.LHMExpand.add
import helper.base.MathHelper
import helper.scxml.scxml2.EnvHelper.RunResult
import helper.scxml.scxml2.EnvHelper.T3BaseEnv.Companion.ifCanNextWhenOneClock
import helper.scxml.scxml2.LearningHelper
import helper.scxml.scxml2.MathHelper.ClockValuations
import helper.scxml.scxml2.MathHelper.Expand.getEuclideanDistance
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.t2_traffic.Res
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toClockValuations
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toLocationEventVListLHM
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toMeanCost
import org.junit.Test

internal class Cycle1Test {
    @Test
    fun t1t1() {
        val env = EnvObjHelper.getEnvObj1()
        val rs = env.toStr()
        println(rs)
    }

    @Test
    fun t1t2t1() {
        val debuggerList = getDebuggerList(
            0,
            1,
        )
        val env = EnvObjHelper.getEnvObj1()
        repeat(100) {
            debuggerList.pln(
                "${"-".repeat(10)}repeat${it}",
                arrayListOf(
                    0,
                    1,
                )
            )
            env.reset()
            val runResult = RunResult()
            env.taskRun2(
                runResult = runResult,
                debuggerList = debuggerList,
            )
            println(runResult)
        }
    }

    @Test
    fun t1t2t2() {
        val debuggerList = getDebuggerList(
            0,
            0,
        )
        val rrs = ArrayList<RunResult>()
        val env = EnvObjHelper.getEnvObj1()
        repeat(100) {
            env.reset()
            env.taskRun(
                debuggerList = debuggerList
            ).let {
                rrs.add(it)
            }
        }
        val sorted = rrs.sortedBy {
            it.endData[Res.globalTimeId]!!.toInt()
        }
        sorted.map {
            println(it)
        }
    }

    @Test
    fun t2t1() {
        val debuggerList = getDebuggerList(
            0,
            0,
        )
        val rrs = ArrayList<RunResult>()
        val env = EnvObjHelper.getEnvObj2()
        repeat(100000) {
            env.reset()
            env.taskRun(
                debuggerList = debuggerList,
            ).let {
                rrs.add(it)
            }
        }
        rrs.map {
            it.endData[Res.globalTimeId]!!.toInt()
        }.average().let {
            println(it)
        }
        //145
    }

    @Test
    fun t3t1() {
        val debuggerList = getDebuggerList(
            0,
            0,
        )
        val rrs = ArrayList<RunResult>()
        val env = EnvObjHelper.getEnvObj3()
        repeat(1000000) {
            println(it)
            env.reset()
            env.taskRun(
                debuggerList = debuggerList,
            ).let {
                rrs.add(it)
            }
        }
        rrs.map {
            it.endData["c"]!!.toInt()
        }.average().let {
            println(it)
        }
        //理论204
        //实际203.223718
    }

    @Test
    fun t4t1() {
        val debuggerList = getDebuggerList(
            0,
            0,
        )
        val hp = LearningHelper.HyperParameterUnit.getObj1()
        var heap = ArrayList<RunResult>()
        val env = EnvObjHelper.getEnvObj1()
        var nowCountOfNoBetter = 0
        var nowCountOfReset = 0
        val meanList = ArrayList<Double>()
        repeat(
            hp.maxIterations
        ) {
            val rrs = ArrayList<RunResult>()
            println("iterations_${it}:\n")
            repeat(hp.maxRuns) {
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
            }.take(hp.maxGood)
            sorted.map {
                heap.add(it)
            }
            heap = heap.sortedBy {
                it.endData["c"]!!.toInt()
            }.take(hp.maxBest).toArrayList()

            val locationEventVListLHM = heap.toLocationEventVListLHM()
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
                                        if (!ifCanNextWhenOneClock(dataXInt, 0 until 100)) return null
                                        val lhm = LinkedHashMap<String, Double>()
                                        locationEventVMeanLHM[stateId]!!.map { (event, mean) ->
                                            lhm.add(
                                                event,
                                                mean.getEuclideanDistance(
                                                    scxmlTuple.toData().toClockValuations(),
                                                ),
                                            )
                                        }
                                        return MathHelper.getRandomString(lhm)
                                    }
                                }
                            }
                        }
                        return null
                    }
                }
            }

            val evaluateResultList = ArrayList<RunResult>()

            repeat(hp.evalRuns) {
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
            println("mean3=${mean1.coerceAtMost(mean2)}")
            if (mean1 < mean2) {
                env.strategyTuple.getRenEventSelectorFun = oldGetIRenEventSelectorFun
                nowCountOfNoBetter += 1
                if (nowCountOfNoBetter >= hp.maxNoBetter) {
                    //重置
                    env.strategyTuple.getRenEventSelectorFun = EnvObjHelper.getIRenEventSelectorFunObj1()
                    nowCountOfNoBetter = 0
                    nowCountOfReset += 1
                    if (nowCountOfReset >= hp.maxResets) {
                        println("nowCountOfReset>=hp.maxResets\t\t${nowCountOfReset}>=${hp.maxResets}")
                        println("minMean=${meanList.minOrNull()}")
                        return
                    }
                }
            }
        }
        println("nowCountOfReset=${nowCountOfReset}")
        println("minMean=${meanList.minOrNull()}")
    }
}