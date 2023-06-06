package helper.scxml.scxml2.t7_cycle

import helper.DebugHelper
import helper.DebugHelper.DebuggerList
import helper.base.LHMHelper.A3LHM
import helper.base.MathHelper
import helper.base.RandomHelper
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.EnvHelper
import helper.scxml.scxml2.Expand.DataExpand.exprToInt
import helper.scxml.scxml2.Expand.DataExpand.setExprAddOne
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.scxml2.StrategyTripleHelper
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import org.apache.commons.scxml2.model.Data
import helper.scxml.scxml2.IDataExpandHelper.Expand.ifMeet

object EnvHelper {
    class Env(
        envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
        envEventLHM: A3LHM<String, String, Double>,
        getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
    ) : EnvHelper.Env(
        envStateConstraintLHM,
        envEventLHM,
        getIRenEventSelectorFun,
    ) {
        override val scxmlTuple = Scxml2Helper.getSCXMLTuple("scxml2/t7_cycle/cycle.scxml").also {
            it.initialStateList.add("s0")
            it.finalStateList.add("s1")
            it.stateClockListLHM["s0"] = arrayListOf("x")
        }

        val dataX: Data
            get() {
                return dataSCXML.getData("x")!!
            }

        val dataXInt: Int
            get() {
                return dataX.exprToInt()
            }

        override val ifDone: Boolean
            get() {
                if (super.ifDone) return true
                if (ifMachineTimeMax) return true
                return false
            }

        fun getEvent(
            debuggerList: DebuggerList,
        ): String? {
            executor.status.activeStates.map {
                it.id
            }.map { stateId ->
                if (isInFinalState()) return@map
                envStateConstraintLHM[stateId]!!.let {
                    if (!it.ifMeet(dataSCXML)) return@map
                    val booleanInProbability = RandomHelper.getBooleanInProbability(it.maxV - dataXInt + 1)
                    if (!booleanInProbability) return@map
                }
                if (envEventLHM.containsKey(stateId)) {
                    envEventLHM[stateId]?.let {
                        MathHelper.getRandomString(it)
                    }?.let {
                        return it
                    }
                }
                getRenEvent(stateId)?.let { event ->
                    return event
                }
            }
            return null
        }

        //如果有event就fire出去
        fun strategyFireEvent(
            debuggerList: DebuggerList,
        ): String? {
            debuggerList.startPln("getEvent")
            val event: String? = getEvent(
                debuggerList = debuggerList,
            )
            debuggerList.endPln()
            if (event != null) {
                //fire出去了
                scxmlTuple.fireEvent(
                    event = event,
                    debuggerList = debuggerList,
                )
                if (!ifOnRenState) {
                    strategyFireEvent(
                        debuggerList = debuggerList,
                    )
                }
            }
            return event
        }

        fun step(
            debuggerList: DebuggerList,
        ) {
            if (ifDone) return

            debuggerList.startPln("strategyFireEvent")

            val event: String? = strategyFireEvent(
                debuggerList = debuggerList,
            )
            debuggerList.endPln()
            if (event != null) {
                return
            }
            debuggerList.pln("addTime")
            dataX.setExprAddOne()
            dataGlobalTime.setExprAddOne()
            if (ifMachineTimeMax) {
                return
            }
        }

        fun stepToNeedRen(
            debuggerList: DebuggerList,
        ) {
            if (ifDone) return
            debuggerList.startPln("stepToNeedRen")
            var hasStepOne = false
            while (true) {
                if (ifDone) return
                if (hasStepOne) {
                    if (ifOnRenState) {
                        debuggerList.pln("ifOnRenState=$ifOnRenState")
                        break
                    }
                }
                debuggerList.startPln("step")
                step(
                    debuggerList,
                )
                debuggerList.endPln()
                if (!hasStepOne) {
                    hasStepOne = true
                }
            }
            debuggerList.endPln()
        }

        fun taskRun(
            debuggerList: DebuggerList = DebugHelper.getDebuggerList(0),
        ) {
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
