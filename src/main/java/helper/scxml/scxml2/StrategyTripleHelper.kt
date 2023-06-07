package helper.scxml.scxml2

import helper.base.LHMHelper.A3LHM
import helper.base.MathHelper
import helper.scxml.ScxmlVarHelper.ClockConstraint

object StrategyTripleHelper {
    //人为事件选择器接口
    interface IRenEventSelector {
        fun getRenEvent(stateId: String): String?
    }

    //只和状态相关
    //人为事件选择器
    class StateRenEventSelector(
        val renEventLHM: A3LHM<String, String, Double> = A3LHM(),
    ) : IRenEventSelector {
        override fun getRenEvent(stateId: String): String? {
            this.renEventLHM[stateId]?.let {
                return MathHelper.getRandomString(it)
            }
            return null
        }
    }

    //这3变量，在运行中是固定不变的
    //时间延迟
    //环境策略
    //人策略
    //子类后面扩展的，都要考虑会不会变
    open class StrategyTriple(
        val envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
        val envEventLHM: A3LHM<String, String, Double>,
        val getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
    ) {
        open fun getRenEvent(
            scxmlTuple: SCXMLTuple,
            stateId: String,
        ): String? {
            return getIRenEventSelectorFun(scxmlTuple).getRenEvent(stateId)
        }
    }

    open class Type2StrategyTriple(
        val envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
        val envEventLHM: A3LHM<String, String, Double>,
        val getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
    ) {
        open fun getRenEvent(
            scxmlTuple: SCXMLTuple,
            stateId: String,
        ): String? {
            return getIRenEventSelectorFun(scxmlTuple).getRenEvent(stateId)
        }
    }
}