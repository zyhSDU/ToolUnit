package helper.scxml.scxml2.t2_traffic.env

import helper.base.DebugHelper.Debugger
import helper.base.DebugHelper.getDebuggerList
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.t2_traffic.Res
import helper.scxml.scxml2.t2_traffic.env.EnvHelper.Env
import helper.scxml.scxml2.t2_traffic.env.EnvObjHelper.getEnvObj1
import helper.scxml.scxml2.t2_traffic.env.EnvObjHelper.getEnvObj2
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.envEventLHM1
import org.junit.Test

internal class Test {
    @Test
    fun t0t1() {
        val env = getEnvObj1()
        val rs = env.toStr()
        println(rs)
    }

    @Test
    fun t0t2() {
        val env = getEnvObj2()
        val rs = env.toStr()
        println(rs)
    }

    private fun getTrafficEnvObj1(
        getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
    ): Env {
        return Env(
            envStateConstraintLHM = StrategyTripleHelper.dEnvStateConstraintLHM,
            envEventLHM = envEventLHM1,
            getIRenEventSelectorFun = getIRenEventSelectorFun,
        )
    }

    //均匀策略
    private fun getTrafficEnvObj2(): Env {
        return getTrafficEnvObj1 {
            StrategyTripleHelper.stateRenEventSelector1
        }
    }

    data class LocationActionClockUnit(
        val location: String,
        val action: String,
        val c1: Double,
        val c2: Double,
    ) {
        fun toStr(): String {
            return "(${location},${action},${c1},${c2})"
        }
    }

    class RunResult(
        val us: ArrayList<LocationActionClockUnit> = ArrayList(),
        var leftTime: Int = 0,
    ) : Comparable<RunResult> {
        //从大到小排
        override fun compareTo(other: RunResult): Int {
            return other.leftTime.compareTo(this.leftTime)
        }

        fun toStr(): String {
            val sb = StringBuilder()
            us.map {
                sb.append("${it.toStr()}\n")
            }
            sb.append("leftTime=${leftTime}")
            return sb.toString()
        }
    }

    fun taskRun1(
        rrs: ArrayList<RunResult> = ArrayList(),
    ) {
        val runResult = RunResult()
        val trafficEnv = getTrafficEnvObj2()
        val leftTime = trafficEnv.taskRun(
            countClockValueFun = { scxmlTuple, event ->
                val state = scxmlTuple.activeStatesString
                if (Res.renStateList.contains(state)) {
                    runResult.us.add(
                        LocationActionClockUnit(
                            state,
                            event,
                            scxmlTuple.dataSCXML.getDataInt(Res.globalTimeId)!!.toDouble(),
                            scxmlTuple.dataSCXML.getDataInt("T")!!.toDouble(),
                        )
                    )
                }
            },
            debuggerList = getDebuggerList(
                Debugger(0),
                Debugger(0),
            ),
        )
        runResult.leftTime = leftTime
        rrs.add(runResult)
    }

    @Test
    fun t1t1() {
        val env = getTrafficEnvObj2()
        val rs = env.toStr()
        println(rs)
    }
}