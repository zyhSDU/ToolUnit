package helper.scxml.scxml2.t5_traffic_train

import helper.base.LHMHelper.A3LHM
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.StrategyTripleHelper.StateRenEventSelector

object StrategyTripleHelper {
    private fun getBaseRandomEnvStateConstraintLHM() = LinkedHashMap<String, ClockConstraint>().also {
        it["Go"] = ClockConstraint("T", 35)
    }

    //确定环境，状态约束LHM
    val dEnvStateConstraintLHM = getBaseRandomEnvStateConstraintLHM().also {
        it["Aalborg"] = ClockConstraint("T", 1)//
        it["Train"] = ClockConstraint("T", 5)//
        it["Wait"] = ClockConstraint("T", 1)//
    }

    private fun getBaseEnvEventLHM() =  A3LHM<String, String, Double>().also {
        it["Go"] = linkedMapOf("train_go_end" to 1.0)
    }

    //环境事件LHM
    val envEventLHM1 = getBaseEnvEventLHM().also {
        it["Train"] = linkedMapOf(
            "train_go" to 10.0,
            "train_wait" to 1.0
        )
    }

    //和数据无关的，
    //人为事件选择器
    val stateRenEventSelector1 = StateRenEventSelector(
        A3LHM<String, String, Double>().also {
            it["Aalborg"] = linkedMapOf(
                "train" to 1.0,
            )
            it["Wait"] = linkedMapOf(
                "wait_train" to 1.0,
            )
        }
    )
}