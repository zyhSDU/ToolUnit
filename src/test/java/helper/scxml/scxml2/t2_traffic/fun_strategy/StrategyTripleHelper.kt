package helper.scxml.scxml2.t2_traffic.fun_strategy

import helper.base.BaseTypeHelper.LHMExpand.getMaxKey
import helper.base.LHMHelper.A3LHM
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.StrategyTripleHelper.StateRenEventSelector
import helper.scxml.scxml2.t2_traffic.fun_strategy.FunStrategyHelper.StrIntStrIntLHM

object StrategyTripleHelper {
    private fun getBaseRandomEnvStateConstraintLHM() = LinkedHashMap<String, ClockConstraint>().also {
        it["Car"] = ClockConstraint("T", 0)
        it["Easy"] = ClockConstraint("T", 20)
        it["Heavy"] = ClockConstraint("T", 140)
        it["Go"] = ClockConstraint("T", 35)
        it["GoBack"] = ClockConstraint("T", 0)
    }

    //随机环境，状态约束LHM
    val rEnvStateConstraintLHM = getBaseRandomEnvStateConstraintLHM().also {
        it["Aalborg"] = ClockConstraint("T", 0..2)//
        it["Bike"] = ClockConstraint("T", 42..45)//
        it["Train"] = ClockConstraint("T", 4..6)//
        it["Wait"] = ClockConstraint("T", 0..2)//
    }

    //确定环境，状态约束LHM
    val dEnvStateConstraintLHM = getBaseRandomEnvStateConstraintLHM().also {
        it["Aalborg"] = ClockConstraint("T", 1)//
        it["Bike"] = ClockConstraint("T", 44)//
        it["Train"] = ClockConstraint("T", 5)//
        it["Wait"] = ClockConstraint("T", 1)//
    }

    fun getBaseEnvEventLHM() = A3LHM<String, String, Double>().also {
        it["Bike"] = linkedMapOf("bike_end" to 1.0)
        it["Easy"] = linkedMapOf("car_easy_end" to 1.0)
        it["Heavy"] = linkedMapOf("car_heavy_end" to 1.0)
        it["Go"] = linkedMapOf("train_go_end" to 1.0)
        it["GoBack"] = linkedMapOf("back_back" to 1.0)
    }

    //环境事件LHM
    //Car,Train需要调
    val envEventLHM1 = getBaseEnvEventLHM().also {
        it["Car"] = linkedMapOf(
            "car_easy" to 10.0,
            "car_heavy" to 1.0,
        )
        it["Train"] = linkedMapOf(
            "train_go" to 10.0,
            "train_wait" to 1.0
        )
    }

    //人为事件选择器
    //1
    val stateRenEventSelector1 = StateRenEventSelector(
        A3LHM<String, String, Double>().also {
            it["Aalborg"] = linkedMapOf(
                "bike" to 1.0,
                "car" to 1.0,
                "train" to 1.0,
            )
            it["Wait"] = linkedMapOf(
                "train_wait_train" to 1.0,
                "train_wait_back" to 1.0,
            )
        }
    )

    //人为事件选择器
    //2
    val stateRenEventSelector2 = StateRenEventSelector(
        A3LHM<String, String, Double>().also {
            it["Aalborg"] = linkedMapOf(
                "bike" to 1.0,
                "train" to 1.0,
            )
            it["Wait"] = linkedMapOf(
                "train_wait_back" to 1.0,
            )
        }
    )

    val stateRenEventSelector3 = StateRenEventSelector(
        A3LHM<String, String, Double>().also {
            it["Aalborg"] = linkedMapOf(
                "car" to 1.0,
            )
            it["Wait"] = linkedMapOf(
                "train_wait_back" to 1.0,
            )
        }
    )

    //和状态、数据有关的选择器
    class StateDataRenEventSelector(
        private val scxmlTuple: SCXMLTuple,
        private val a4LHM: StrIntStrIntLHM,
    ) : IRenEventSelector {
        override fun getEvent(
            stateId: String,
        ): String? {
            a4LHM[stateId]?.let {
                it[scxmlTuple.dataSCXML.getDataInt("retryTrainCount")]?.getMaxKey()
            }?.let {
                return it
            }
            //实在找不到，则出动随机
            println("cannot getRenEvent_learned,than getRenEvent_random")
            return stateRenEventSelector1.getEvent(stateId)
        }

        object Expand {
            fun StrIntStrIntLHM.toLearnedRenEventSelector(
                scxmlTuple: SCXMLTuple,
            ): StateDataRenEventSelector {
                return StateDataRenEventSelector(scxmlTuple, this)
            }
        }
    }
}