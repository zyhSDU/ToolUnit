package helper.scxml.scxml2.t7_cycle

import helper.DebugHelper.DebuggerList
import helper.DebugHelper.getDebuggerList
import helper.base.LHMHelper.A3LHM
import helper.base.PrintHelper.StringTo.toPrintln
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.scxml2.StrategyTripleHelper
import helper.scxml.scxml2.t7_cycle.Cycle1Test.EnvHelper.RunResult
import org.junit.Test

internal class Cycle1Test {
    object EnvHelper {
        data class RunResult(
            var endTime: Int = 0
        )

        class Env(
            envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
            envEventLHM: A3LHM<String, String, Double>,
            getIRenEventSelectorFun: (SCXMLTuple) -> StrategyTripleHelper.IRenEventSelector,
        ) : helper.scxml.scxml2.EnvHelper.T2BaseEnv(
            envStateConstraintLHM,
            envEventLHM,
            getIRenEventSelectorFun,
        ) {
            override val scxmlTuple = Scxml2Helper.getSCXMLTuple("scxml2/t7_cycle/cycle1.scxml").also {
                it.initialStateList.add("s0")
                it.finalStateList.add("s1")
                it.stateNeedConsiderClockListLHM["s0"] = arrayListOf("x")
                it.stateDataIncrementLHM.add("s0", "x", 1.0)
            }

            fun taskRun2(
                debuggerList: DebuggerList = getDebuggerList(0),
            ): RunResult {
                val runResult = RunResult()

                fun debugPlnStatus() {
                    debuggerList.pln(
                        statusString,
                        arrayListOf(0, 1),
                    )
                }

                reset()
                executor.go()

                debugPlnStatus()
                while (true) {
                    if (ifDone) break
                    stepToNeedRen(
                        debuggerList,
                    )
                }
                debugPlnStatus()

                runResult.endTime = dataGlobalTimeInt
                return runResult
            }
        }

        fun getEnvObj1(): Env {
            return Env(
                envStateConstraintLHM = LinkedHashMap<String, ClockConstraint>().also {
                    it["s0"] = ClockConstraint("x", 0..100)
                },
                envEventLHM = A3LHM<String, String, Double>().also {
                    it["s0"] = linkedMapOf("s0s1" to 1.0)
                },
                getIRenEventSelectorFun = {
                    StrategyTripleHelper.StateRenEventSelector()
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
        repeat(10) {
            debuggerList.pln(
                "${"-".repeat(10)}repeat${it}",
                arrayListOf(
                    0,
                    1,
                )
            )
            env.reset()
            val rr: RunResult = env.taskRun2(
                debuggerList
            )
            println(rr)
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
        repeat(100000) {
            env.reset()
            val rr = env.taskRun2(
                debuggerList
            )
            rrs.add(rr)
        }
        val sorted = rrs.map {
            it.endTime
        }.sorted()
        val lhm = LinkedHashMap<Int, Int>()
        sorted.map {
            if (!lhm.containsKey(it)) {
                lhm[it] = 0
            }
            lhm[it] = lhm[it]!! + 1
        }
        lhm.map { (k, v) ->
            println("${k}:${v}")
        }
    }
}