package helper.scxml.scxml2.t7_cycle

import helper.DebugHelper.getDebuggerList
import helper.base.LHMHelper.LHMExpand.add
import helper.base.MathHelper
import helper.scxml.scxml2.EnvHelper.RunResult
import helper.scxml.scxml2.EnvHelper.T3BaseEnv
import helper.scxml.scxml2.EnvHelper.T3BaseEnv.Companion.ifCanNextWhenOneClock
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.scxml2.StrategyTripleHelper
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.t2_traffic.Res
import org.junit.Test

internal class Cycle1Test {
    object EnvHelper {
        class Env(
            override val strategyTuple: StrategyTripleHelper.Type2StrategyTuple,
            private val machineTimeMax: Int,
        ) : T3BaseEnv() {
            override val scxmlTuple = Scxml2Helper.getSCXMLTuple("scxml2/t7_cycle/cycle1.scxml").also {
                it.initialStateList.add("s0")
                it.renStateList.add("s1")
                it.finalStateList.add("s4")
                arrayListOf("s0", "s1", "s2", "s3").map { stateId ->
                    it.stateNeedConsiderClockListLHM.add(stateId, arrayListOf("x"))
                }
                arrayListOf("s0", "s1", "s2", "s3").map { stateId ->
                    it.stateDataIncrementLHM.add(stateId, "x", 1.0)
                }
                it.stateDataIncrementLHM.run {
                    add("s0", "x", 1.0)
                    add("s1", "c", 4.0)
                    add("s2", "c", 3.0)
                    add("s3", "c", 2.0)
                }
            }

            override val ifMachineTimeMax: Boolean
                get() {
                    return dataGlobalTimeInt >= machineTimeMax
                }
        }

        fun getEnvObj1(): Env {
            return Env(
                machineTimeMax = Int.MAX_VALUE,
                strategyTuple = StrategyTripleHelper.Type2StrategyTuple(
                    getIEnvEventSelectorFun = { scxmlTuple ->
                        object : StrategyTripleHelper.IEnvEventSelector {
                            override fun getEvent(stateId: String): String? {
                                val dataXInt: Int = scxmlTuple.dataSCXML.getDataInt("x")!!
                                when (stateId) {
                                    "s0" -> {
                                        if (!ifCanNextWhenOneClock(dataXInt, 0..100)) return null
                                        linkedMapOf("s0s1" to 1.0).let {
                                            return MathHelper.getRandomString(it)
                                        }
                                    }
                                    "s1" -> {
                                        if (!ifCanNextWhenOneClock(dataXInt, 90 until 100)) return null
                                        linkedMapOf("s1s4t2" to 1.0).let {
                                            return MathHelper.getRandomString(it)
                                        }
                                    }
                                    "s2" -> {
                                        if (!ifCanNextWhenOneClock(dataXInt, 60..120)) return null
                                        linkedMapOf("s2s4" to 1.0).let {
                                            return MathHelper.getRandomString(it)
                                        }
                                    }
                                    "s3" -> {
                                        if (!ifCanNextWhenOneClock(dataXInt, 20..140)) return null
                                        linkedMapOf("s3s4" to 1.0).let {
                                            return MathHelper.getRandomString(it)
                                        }
                                    }
                                }
                                return null
                            }
                        }
                    },
                    getIRenEventSelectorFun = { scxmlTuple ->
                        object : IRenEventSelector {
                            override fun getEvent(stateId: String): String? {
                                val dataXInt = scxmlTuple.dataSCXML.getDataInt("x")!!
                                when (stateId) {
                                    "s1" -> {
                                        when (dataXInt) {
                                            100 -> {
                                                linkedMapOf(
                                                    "s1s2" to 1.0,
                                                    "s1s3" to 1.0,
                                                    "s1s4t1" to 1.0,
                                                ).let {
                                                    return MathHelper.getRandomString(it)
                                                }
                                            }
                                            else -> {
                                                if (!ifCanNextWhenOneClock(dataXInt, 0..100)) return null
                                                linkedMapOf(
                                                    "s1s2" to 1.0,
                                                    "s1s3" to 1.0,
                                                ).let {
                                                    return MathHelper.getRandomString(it)
                                                }
                                            }
                                        }
                                    }
                                }
                                return null
                            }
                        }
                    },
                ),
            )
        }
    }

    @Test
    fun t1t1() {
        val env = EnvHelper.getEnvObj1()
        val rs = env.toStr()
        println(rs)
    }

    @Test
    fun t1t2t1() {
        val debuggerList = getDebuggerList(
            0,
            1,
        )
        val env = EnvHelper.getEnvObj1()
        repeat(1) {
            debuggerList.pln(
                "${"-".repeat(10)}repeat${it}",
                arrayListOf(
                    0,
                    1,
                )
            )
            env.reset()
            val runResult = env.taskRun(debuggerList)
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
        val env = EnvHelper.getEnvObj1()
        repeat(100) {
            env.reset()
            val rr = env.taskRun(
                debuggerList
            )
            rrs.add(rr)
        }
        val sorted = rrs.sortedBy {
            it.dataLHM[Res.globalTimeId]!!.toInt()
        }
        sorted.map {
            println(it)
        }
    }
}