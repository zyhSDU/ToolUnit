package helper.scxml.scxml2.t7_cycle

import helper.DebugHelper.getDebuggerList
import helper.base.LHMHelper.A3LHM
import helper.base.LHMHelper.LHMExpand.add
import helper.base.MathHelper
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.t2_traffic.Res
import org.junit.Test

internal class Cycle2Test {
    object EnvHelper {
        class Env(
            envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
            envEventLHM: A3LHM<String, String, Double>,
            getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
        ) : helper.scxml.scxml2.EnvHelper.T2BaseEnv(
            envStateConstraintLHM,
            envEventLHM,
            getIRenEventSelectorFun,
        ) {
            override val scxmlTuple = Scxml2Helper.getSCXMLTuple("scxml2/t7_cycle/cycle2.scxml").also {
                it.initialStateList.add("s0")
                it.finalStateList.add("s4")
                arrayListOf("s0", "s1", "s2", "s3").map { stateId ->
                    it.stateNeedConsiderClockListLHM.add(stateId, arrayListOf("x"))
                }
                arrayListOf("s0", "s1", "s2", "s3").map { stateId ->
                    it.stateDataIncrementLHM.add(stateId, "x", 1.0)
                }
                it.stateDataIncrementLHM.add("s0", "x", 1.0)
                it.stateDataIncrementLHM.add("s1", "c", 4.0)
                it.stateDataIncrementLHM.add("s2", "c", 3.0)
                it.stateDataIncrementLHM.add("s3", "c", 2.0)
            }

            private val machineTimeMax = 240

            override val ifMachineTimeMax: Boolean
                get() {
                    return dataGlobalTimeInt >= machineTimeMax
                }
        }

        fun getEnvObj1(): Env {
            return Env(
                envStateConstraintLHM = LinkedHashMap<String, ClockConstraint>().also {
                    it["s0"] = ClockConstraint("x", 0..100)
                    it["s1"] = ClockConstraint("x", 0..100)
                    it["s2"] = ClockConstraint("x", 60..120)
                    it["s3"] = ClockConstraint("x", 20..140)
                },
                envEventLHM = A3LHM<String, String, Double>().also {
                    it["s0"] = linkedMapOf("s0s1" to 1.0)
                    it["s2"] = linkedMapOf("s2s4" to 1.0)
                    it["s3"] = linkedMapOf("s3s4" to 1.0)
                },
                getIRenEventSelectorFun = { scxmlTuple ->
                    object : IRenEventSelector {
                        override fun getRenEvent(stateId: String): String? {
                            if (stateId == "s1") {
                                val dataInt = scxmlTuple.dataSCXML.getDataInt("x")!!
                                when (dataInt) {
                                    100 -> {
                                        linkedMapOf(
                                            "s1s2" to 1.0,
                                            "s1s3" to 1.0,
                                            "s1s4t1" to 1.0,
                                            "s1s4t2" to 1.0,
                                        ).let {
                                            return MathHelper.getRandomString(it)
                                        }
                                    }
                                    in 90 until 100 -> {
                                        linkedMapOf(
                                            "s1s2" to 1.0,
                                            "s1s3" to 1.0,
                                            "s1s4t2" to 1.0,
                                        ).let {
                                            return MathHelper.getRandomString(it)
                                        }
                                    }
                                    in 0 until 90 -> {
                                        linkedMapOf(
                                            "s1s2" to 1.0,
                                            "s1s3" to 1.0,
                                        ).let {
                                            return MathHelper.getRandomString(it)
                                        }
                                    }
                                }
                            }
                            return null
                        }
                    }
                }
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
        val rrs = ArrayList<helper.scxml.scxml2.EnvHelper.RunResult>()
        val env = EnvHelper.getEnvObj1()
        repeat(100) {
            env.reset()
            val rr = env.taskRun(
                debuggerList
            )
            rrs.add(rr)
        }
        val sorted = rrs.sortedBy {
            it[Res.globalTimeId]!!.toInt()
        }
        sorted.map {
            println(it)
        }
    }
}