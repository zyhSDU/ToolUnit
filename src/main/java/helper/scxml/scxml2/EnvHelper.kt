package helper.scxml.scxml2

import helper.base.LHMHelper.A3LHM
import helper.base.LHMHelper.LHMExpand.toStr
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.Expand.DataExpand.exprToInt
import helper.scxml.scxml2.Expand.SCXMLExecutorExpand.isInState
import helper.scxml.scxml2.Expand.ToStr.toStr
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.StrategyTripleHelper.StateRenEventSelector
import helper.scxml.scxml2.StrategyTripleHelper.StrategyTriple
import org.apache.commons.scxml2.SCXMLExecutor
import org.apache.commons.scxml2.model.Data

object EnvHelper {
    abstract class Env(
        envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
        envEventLHM: A3LHM<String, String, Double>,
        getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
    ) : StrategyTriple(
        envStateConstraintLHM,
        envEventLHM,
        getIRenEventSelectorFun,
    ) {
        abstract val scxmlTuple: SCXMLTuple

        val executor: SCXMLExecutor
            get() = scxmlTuple.executor

        val dataSCXML: DataSCXML
            get() = scxmlTuple.dataSCXML

        val activeStatesString: String
            get() {
                return scxmlTuple.activeStatesString
            }

        val dataGlobalTime: Data
            get() {
                return dataSCXML.getData(Res.globalTimeId)!!
            }

        val dataGlobalTimeInt: Int
            get() {
                return dataGlobalTime.exprToInt()
            }

        val ifOnRenState: Boolean
            get() {
                return scxmlTuple.renStateList.contains(activeStatesString)
            }

        val statusString: String
            get() {
                return scxmlTuple.getStatusString()
            }

        fun getRenEvent(
            stateId: String,
        ): String? {
            return getRenEvent(scxmlTuple, stateId)
        }

        fun reset() {
            executor.reset()
            dataSCXML.reset()
        }

        fun toStr(
            tabNum: Int = 0,
        ): String {
            val tabNum1 = tabNum + 1
            val tabNum2 = tabNum + 2
            val tabNumStr = "\t".repeat(tabNum)
            val tabNumStr1 = "\t".repeat(tabNum1)
            val sb = StringBuilder()
            sb.append("${tabNumStr}env:\n")
            sb.append(this.scxmlTuple.toStr(tabNum1))
            sb.append("${tabNumStr1}envStateConstraintLHM:\n")
            sb.append(envStateConstraintLHM.toStr(tabNum2))
            sb.append("${tabNumStr1}envEventLHM:\n")
            sb.append(envEventLHM.toStr(tabNum2))
            sb.append("${tabNumStr1}getIRenEventSelectorFun:")
            val iRenEventSelector = getIRenEventSelectorFun(scxmlTuple)
            if (iRenEventSelector is StateRenEventSelector) {
                sb.append("isStateRenEventSelector:\n")
                sb.append(iRenEventSelector.renEventLHM.toStr(tabNum2))
            } else {
                sb.append("\n")
            }
            return sb.toString()
        }

        fun isInFinalState(): Boolean {
            scxmlTuple.finalStateList.map {
                if (executor.isInState(it)) return true
            }
            return false
        }

        open val ifMachineTimeMax: Boolean
            get() {
                return false
            }

        open val ifDone: Boolean
            get() {
                if (isInFinalState()) return true
                return false
            }
    }
}