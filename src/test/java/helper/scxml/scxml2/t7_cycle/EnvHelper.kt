package helper.scxml.scxml2.t7_cycle

import helper.base.LHMHelper.A3LHM
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.EnvHelper
import helper.scxml.scxml2.Expand.DataExpand.exprToInt
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.scxml2.StrategyTripleHelper
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import org.apache.commons.scxml2.model.Data

object EnvHelper {
    class Env(
        envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
        envEventLHM: A3LHM<String, String, Double>,
        getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
    ) : EnvHelper.T2BaseEnv(
        envStateConstraintLHM,
        envEventLHM,
        getIRenEventSelectorFun,
    ) {
        override val scxmlTuple = Scxml2Helper.getSCXMLTuple("scxml2/t7_cycle/cycle.scxml").also {
            it.initialStateList.add("s0")
            it.finalStateList.add("s1")
            it.stateNeedAddOneClockListLHM["s0"] = arrayListOf("x")
        }

        val dataX: Data
            get() {
                return dataSCXML.getData("x")!!
            }

        val dataXInt: Int
            get() {
                return dataX.exprToInt()
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
