package helper.scxml.scxml2

import helper.base.LHMHelper.A3LHM
import helper.base.MathHelper
import helper.scxml.ScxmlVarHelper.ClockConstraint

object StrategyTripleHelper {
    interface IEventSelector {
        fun getEvent(stateId: String): String?
        fun toStr(): String {
            return "IEventSelector"
        }
    }

    //环境事件选择器接口
    interface IEnvEventSelector : IEventSelector

    //人为事件选择器接口
    interface IRenEventSelector : IEventSelector

    //只和状态相关
    //人为事件选择器
    class StateRenEventSelector(
        val renEventLHM: A3LHM<String, String, Double> = A3LHM(),
    ) : IRenEventSelector {
        override fun getEvent(stateId: String): String? {
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
    open class StrategyTuple(
        val envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
        val envEventLHM: A3LHM<String, String, Double>,
        val getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
    ) {
        open fun getRenEvent(
            scxmlTuple: SCXMLTuple,
            stateId: String,
        ): String? {
            return getIRenEventSelectorFun(scxmlTuple).getEvent(stateId)
        }
    }

    interface IStrategyTuple {
        val getEnvEventSelectorFun: (SCXMLTuple) -> IEnvEventSelector
        val getRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector
    }

    open class Type2StrategyTuple(
        override val getEnvEventSelectorFun: (SCXMLTuple) -> IEnvEventSelector,
        override var getRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
    ) : IStrategyTuple {
        open fun getEnvEvent(
            scxmlTuple: SCXMLTuple,
            stateId: String,
        ): String? {
            return getEnvEventSelectorFun(scxmlTuple).getEvent(stateId)
        }

        open fun getRenEvent(
            scxmlTuple: SCXMLTuple,
            stateId: String,
        ): String? {
            return getRenEventSelectorFun(scxmlTuple).getEvent(stateId)
        }
    }
}