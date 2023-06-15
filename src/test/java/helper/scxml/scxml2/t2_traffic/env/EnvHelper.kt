package helper.scxml.scxml2.t2_traffic.env

import helper.base.DebugHelper.DebuggerList
import helper.base.DebugHelper.DebuggerList.Companion.getDebuggerList
import helper.base.LHMHelper.A3LHM
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml0.TrafficTestHelper
import helper.scxml.scxml2.EnvHelper
import helper.scxml.scxml2.Expand.DataExpand.exprToInt
import helper.scxml.scxml2.Expand.DataExpand.setExprAddOne
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.t2_traffic.Res
import helper.scxml.scxml2.t2_traffic.fun_strategy.FunStrategyHelper
import org.apache.commons.scxml2.model.Data
import org.apache.commons.scxml2.model.TransitionTarget
import java.io.File

object EnvHelper {
    class Env(
        envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
        envEventLHM: A3LHM<String, String, Double>,
        getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
    ) : EnvHelper.T1BaseEnv(
        envStateConstraintLHM,
        envEventLHM,
        getIRenEventSelectorFun,
    ) {
        override val scxmlTuple = TrafficTestHelper.getSCXMLTuple().also {
            it.initialStateList.add("Aalborg")
            arrayListOf("Aalborg", "Wait").map { s ->
                it.renStateList.add(s)
            }
            it.finalStateList.add("Sydney")
        }

        val dataT: Data
            get() {
                return dataSCXML.getData("T")!!
            }

        val dataTInt: Int
            get() {
                return dataT.exprToInt()
            }

        val dataRetryTrainCount: Data
            get() {
                return dataSCXML.getData("retryTrainCount")!!
            }

        val dataRetryTrainCountInt: Int
            get() {
                return dataRetryTrainCount.exprToInt()
            }

        private val machineTimeMax = 60

        override val ifMachineTimeMax: Boolean
            get() {
                return dataGlobalTimeInt >= machineTimeMax
            }

        //具体运行时，需要记录每个状态的上次被进入的全局时间
        val stateEnterTimeLHM = LinkedHashMap<String, Int>().also {
            it[scxmlTuple.initialStateList[0]] = 0
        }

        val doOnEntry = { it: TransitionTarget ->
            stateEnterTimeLHM[it.id] = dataSCXML.getDataInt(Res.globalTimeId)!!
        }

        val firedStateIdEventList = ArrayList<FunStrategyHelper.SIISUnit>()

        fun reset(
            rlTransition: RLTransition,
        ) {
            reset()
            rlTransition.nextRLState = RLState(scxmlTuple.activeStatesString)
            rlTransition.readyForNext()
        }

        //如果有event就fire出去
        fun strategyFireEvent(
            rlTransition: RLTransition,
            countClockValueFun: (SCXMLTuple, String) -> Unit,
            debuggerList: DebuggerList,
        ): String? {
            debuggerList.startPln("getEvent")
            val event: String? = FunStrategyHelper.getEvent(
                scxmlTuple = scxmlTuple,
                envStateConstraintLHM = envStateConstraintLHM,
                envEventLHM = envEventLHM,
                renEventSelector = getIRenEventSelectorFun(scxmlTuple),
                firedStateIdEventList = firedStateIdEventList,
                stateEnterTimeLHM = stateEnterTimeLHM,
                debuggerList = debuggerList,
            )
            debuggerList.endPln()
            if (event != null) {
                //fire出去了
                scxmlTuple.fireEvent(
                    event,
                    doOnEntry,
                    countClockValueFun,
                    debuggerList,
                )
                if (!ifOnRenState) {
                    strategyFireEvent(
                        rlTransition = rlTransition,
                        countClockValueFun = countClockValueFun,
                        debuggerList = debuggerList,
                    )
                }
            }
            return event
        }

        fun step(
            rlTransition: RLTransition,
            countClockValueFun: (SCXMLTuple, String) -> Unit,
            debuggerList: DebuggerList,
        ) {
            if (rlTransition.done) return

            debuggerList.startPln("strategyFireEvent")

            val event: String? = strategyFireEvent(
                rlTransition = rlTransition,
                countClockValueFun = countClockValueFun,
                debuggerList = debuggerList,
            )
            debuggerList.endPln()
            if (event != null) {
                if (Res.renEventList.contains(event)) {
                    rlTransition.event = event
                }
                rlTransition.nextRLState.machineState = scxmlTuple.activeStatesString
                //有状态离开才有retryTrainCount更新
                rlTransition.nextRLState.retryTimes = dataRetryTrainCountInt
                if (isInFinalState()) {
                    rlTransition.done = true
                    rlTransition.reward = machineTimeMax - dataGlobalTimeInt
                }
                return
            }
            debuggerList.pln("addTime")
            dataT.setExprAddOne()
            dataGlobalTime.setExprAddOne()
            rlTransition.nextRLState.dataTInt = dataTInt
            rlTransition.nextRLState.dataGlobalTimeInt = dataGlobalTimeInt
            if (ifMachineTimeMax) {
                rlTransition.done = true
                rlTransition.reward = -machineTimeMax * 1000000
                return
            }
        }

        fun stepToNeedRen(
            rlTransition: RLTransition,
            countClockValueFun: (SCXMLTuple, String) -> Unit,
            debuggerList: DebuggerList,
        ) {
            if (rlTransition.done) return
            debuggerList.startPln("stepToNeedRen")
            var hasStepOne = false
            while (true) {
                if (rlTransition.done) break
                if (hasStepOne) {
                    val ifOnRenState = ifOnRenState
                    if (ifOnRenState) {
                        debuggerList.pln("ifOnRenState=$ifOnRenState")
                        break
                    }
                }
                debuggerList.startPln("step")
                step(
                    rlTransition,
                    countClockValueFun,
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
            outTransitionFile: File? = null,
            countStateSet: HashSet<String>? = null,
            countClockValueFun: (SCXMLTuple, String) -> Unit = { _, _ -> },
            debuggerList: DebuggerList = getDebuggerList(0),
        ): Int {
            val rlTransition = RLTransition()

            reset(rlTransition)
            executor.go()

            fun debugPlnStatus() {
                debuggerList.pln(
                    statusString,
                    arrayListOf(0, 1),
                )
            }

            debugPlnStatus()
            while (true) {
                if (rlTransition.done) break
                rlTransition.nextRLState.machineState = scxmlTuple.activeStatesString
                stepToNeedRen(
                    rlTransition,
                    countClockValueFun,
                    debuggerList,
                )
                val ifNeedEmptyEventTransition = false
                val isEmpty = rlTransition.event.isEmpty()
                if (ifNeedEmptyEventTransition || !isEmpty) {
                    debuggerList.pln(
                        "rlTransition=${rlTransition}",
                        arrayListOf(0, 1),
                    )
                    if (outTransitionFile != null) {
                        rlTransition.writeToFile(outTransitionFile)
                    }
                    if (countStateSet != null) {
                        countStateSet.add(rlTransition.rlState.machineState)
                        countStateSet.add(rlTransition.nextRLState.machineState)
                    }
                }
                rlTransition.readyForNext()
            }
            debugPlnStatus()
            return machineTimeMax - scxmlTuple.dataSCXML.getDataInt(Res.globalTimeId)!!
        }
    }
}
