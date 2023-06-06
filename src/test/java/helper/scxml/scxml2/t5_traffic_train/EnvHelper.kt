package helper.scxml.scxml2.t5_traffic_train

import helper.DebugHelper.Debugger
import helper.DebugHelper.DebuggerList
import helper.DebugHelper.getDebuggerList
import helper.base.LHMHelper.A3LHM
import helper.base.MathHelper
import helper.base.RandomHelper
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.EnvHelper
import helper.scxml.scxml2.Expand.DataExpand.exprToInt
import helper.scxml.scxml2.Expand.DataExpand.setExprAddOne
import helper.scxml.scxml2.Expand.SCXMLExecutorExpand.isInState
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.ZoneHelper.ZoneState
import org.apache.commons.scxml2.model.Data
import org.apache.commons.scxml2.model.TransitionTarget

object EnvHelper {
    data class SIISUnit(
        val stateId: String,
        val gTime: Int,
        val retry: Int,
        val event: String,
    )

    class Env(
        envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
        envEventLHM: A3LHM<String, String, Double>,
        getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
    ) : EnvHelper.Env(
        envStateConstraintLHM,
        envEventLHM,
        getIRenEventSelectorFun,
    ) {
        override val scxmlTuple = Scxml2Helper.getSCXMLTuple("scxml2/traffic_train.scxml").also {
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

        //具体运行时，需要记录每个状态的上次被进入的全局时间
        val stateEnterTimeLHM = LinkedHashMap<String, Int>().also {
            it[scxmlTuple.initialStateList[0]] = 0
        }

        val doOnEntry = { it: TransitionTarget ->
            stateEnterTimeLHM[it.id] = dataSCXML.getDataInt(Res.globalTimeId)!!
        }

        val firedStateIdEventList = ArrayList<SIISUnit>()

        fun reset(
            rlTransition: RLTransition,
        ) {
            reset()
            rlTransition.nextRLState = RLState(scxmlTuple.activeStatesString)
            rlTransition.readyForNext()
        }

        fun getEvent(
            firedStateIdEventList: ArrayList<SIISUnit>,
            stateEnterTimeLHM: LinkedHashMap<String, Int>,
            debuggerList: DebuggerList,
        ): String? {
            val renEventSelector = getIRenEventSelectorFun(scxmlTuple)
            val dataT: Int = dataSCXML.getDataInt("T")!!
            executor.status.activeStates.map {
                it.id
            }.map { stateId ->
                debuggerList.pln("stateId=${stateId}")
                if (stateId == "Sydney") return@map
                envStateConstraintLHM[stateId]!!.let {
                    if (!it.ifMeet(dataT)) return@map
                    val probability = it.maxV - dataT + 1
                    val booleanInProbability = RandomHelper.getBooleanInProbability(probability)
                    debuggerList.pln("it.maxV=${it.maxV}")
                    debuggerList.pln("dataT=${dataT}")
                    debuggerList.pln("probability=${probability}")
                    debuggerList.pln("booleanInProbability=${booleanInProbability}")
                    if (!booleanInProbability) return@map
                }
                if (envEventLHM.containsKey(stateId)) {
                    envEventLHM[stateId]?.let {
                        MathHelper.getRandomString(it)
                    }?.let {
                        return it
                    }
                }
                renEventSelector.getRenEvent(stateId)?.let { event ->
                    if (Res.renStateList.contains(stateId)) {
                        firedStateIdEventList.add(
                            SIISUnit(
                                stateId,
                                stateEnterTimeLHM[stateId]!!,
                                scxmlTuple.dataSCXML.getDataInt("retryTrainCount")!!,
                                event,
                            )
                        )
                    }
                    return event
                }
            }
            return null
        }

        //如果有event就fire出去
        fun strategyFireEvent(
            rlTransition: RLTransition,
            debuggerList: DebuggerList,
        ): String? {
            debuggerList.startPln("getEvent")
            val event: String? = getEvent(
                firedStateIdEventList = firedStateIdEventList,
                stateEnterTimeLHM = stateEnterTimeLHM,
                debuggerList = debuggerList,
            )
            debuggerList.endPln()
            if (event != null) {
                //fire出去了
                scxmlTuple.fireEvent(
                    event = event,
                    doOnEntryFun = doOnEntry,
                    debuggerList = debuggerList,
                )
                if (!ifOnRenState) {
                    strategyFireEvent(
                        rlTransition = rlTransition,
                        debuggerList = debuggerList,
                    )
                }
            }
            return event
        }

        fun step(
            rlTransition: RLTransition,
            debuggerList: DebuggerList,
        ) {
            if (rlTransition.done) return

            debuggerList.startPln("strategyFireEvent")

            val event: String? = strategyFireEvent(
                rlTransition = rlTransition,
                debuggerList = debuggerList,
            )
            debuggerList.endPln()
            if (event != null) {
                if (Res.renEventList.contains(event)) {
                    rlTransition.event = event
                }
                rlTransition.nextRLState.machineState = activeStatesString
                //有状态离开才有retryTrainCount更新
                rlTransition.nextRLState.retryTimes = dataRetryTrainCountInt
                if (executor.isInState("Sydney")) {
                    rlTransition.done = true
                    rlTransition.reward = 60 - dataGlobalTimeInt
                }
                return
            }
            debuggerList.pln("addTime")
            dataT.setExprAddOne()
            dataGlobalTime.setExprAddOne()
            rlTransition.nextRLState.dataTInt = dataTInt
            rlTransition.nextRLState.dataGlobalTimeInt = dataGlobalTimeInt
            if (dataGlobalTimeInt == 60) {
                rlTransition.done = true
                rlTransition.reward = -60 * 1000000
                return
            }
        }

        fun stepToNeedRen(
            rlTransition: RLTransition,
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
            debuggerList: DebuggerList = getDebuggerList(Debugger(0)),
        ) {
            val rlTransition = RLTransition()

            reset(rlTransition)
            executor.go()

            fun getStatusString(): String {
                return scxmlTuple.getStatusString()
            }

            fun debugPlnStatus() {
                debuggerList.pln(
                    getStatusString(),
                    arrayListOf(0, 1),
                )
            }

            debugPlnStatus()
            while (true) {
                if (rlTransition.done) break
                rlTransition.nextRLState.machineState = scxmlTuple.activeStatesString
                stepToNeedRen(
                    rlTransition,
                    debuggerList,
                )
                val ifNeedEmptyEventTransition = false
                val isEmpty = rlTransition.event.isEmpty()
                if (ifNeedEmptyEventTransition || !isEmpty) {
                    debuggerList.pln(
                        "rlTransition=${rlTransition}",
                        arrayListOf(0, 1),
                    )
                }
                rlTransition.readyForNext()
            }
            debugPlnStatus()
        }

        fun getNextZone(
            nowZoneState: ZoneState
        ): ArrayList<ZoneState> {
            return arrayListOf()
        }
    }
}