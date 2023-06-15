package helper.scxml.scxml2.t5_traffic_train

import helper.base.DebugHelper.DebuggerList.Companion.getDebuggerList
import helper.scxml.scxml2.Expand.ToStr.toStr
//t2不对
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.getBaseEnvEventLHM
import helper.scxml.scxml2.t5_traffic_train.EnvHelper.Env
import org.junit.Test

internal class Test {
    object Expand {
        fun Env.toStr(): String {
            val sb = StringBuilder()
            sb.append(this.scxmlTuple.toStr())
            return sb.toString()
        }
    }

    @Test
    fun t1t1() {
        val trafficEnvObj1 = EnvObjHelper.getTrafficEnvObj1()
        val rs = trafficEnvObj1.toStr()
        println(rs)
    }

    @Test
    fun t1t2() {
        EnvObjHelper.getTrafficEnvObj1().taskRun(
            debuggerList = getDebuggerList(
                0,
                1,
            ),
        )
    }

    private fun getTrafficEnvObj2() = Env(
        envStateConstraintLHM = StrategyTripleHelper.dEnvStateConstraintLHM,
        envEventLHM = getBaseEnvEventLHM().also {
            it["Train"] = linkedMapOf(
                "train_go" to 10.0,
                "train_wait" to 20.0
            )
        },
        getIRenEventSelectorFun = {
            StrategyTripleHelper.stateRenEventSelector1
        }
    )

    @Test
    fun t2t1() {
        getTrafficEnvObj2().taskRun(
            debuggerList = getDebuggerList(
                1,
                0,
            ),
        )
    }

    //将状态机图转为zone格局图
    //再根据格局图统计期望
    @Test
    fun t3t1() {
        val trafficEnvObj1 = EnvObjHelper.getTrafficEnvObj1()
    }
}