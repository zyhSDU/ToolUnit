package helper.scxml.scxml2.t7_cycle

import helper.DebugHelper.getDebuggerList
import helper.base.LHMHelper
import helper.scxml.ScxmlVarHelper
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.scxml2.StrategyTripleHelper
import org.junit.Test

internal class Cycle2Test {
    object EnvHelper {
        class Env(
            envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
            envEventLHM: LHMHelper.A3LHM<String, String, Double>,
            getIRenEventSelectorFun: (SCXMLTuple) -> StrategyTripleHelper.IRenEventSelector,
        ) : helper.scxml.scxml2.EnvHelper.T2BaseEnv(
            envStateConstraintLHM,
            envEventLHM,
            getIRenEventSelectorFun,
        ) {
            override val scxmlTuple = Scxml2Helper.getSCXMLTuple("scxml2/t7_cycle/cycle2.scxml").also {
                it.initialStateList.add("s0")
                it.finalStateList.add("s1")
                it.stateNeedAddOneClockListLHM["s0"] = arrayListOf("x")
            }
        }

        fun getEnvObj1(): Env {
            return Env(
                envStateConstraintLHM = LinkedHashMap<String, ClockConstraint>().also {
                    it["s0"] = ClockConstraint("x", 0..100)
                },
                envEventLHM = LHMHelper.A3LHM<String, String, Double>().also {
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
    fun t1t2() {
        val debuggerList = getDebuggerList(
            0,
            1,
        )
        val env = EnvHelper.getEnvObj1()
        repeat(10) {
            debuggerList.pln("${"-".repeat(10)}repeat${it}", arrayListOf(0, 1))
            env.reset()
            env.taskRun(debuggerList)
        }
    }
}