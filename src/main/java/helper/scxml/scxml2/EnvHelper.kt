package helper.scxml.scxml2

import helper.DebugHelper
import helper.base.LHMHelper.A3LHM
import helper.base.LHMHelper.LHMExpand.toStr
import helper.base.MathHelper
import helper.base.RandomHelper
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.Expand.DataExpand.exprToInt
import helper.scxml.scxml2.Expand.DataExpand.setExprAddOne
import helper.scxml.scxml2.Expand.SCXMLExecutorExpand.isInState
import helper.scxml.scxml2.Expand.ToStr.toStr
import helper.scxml.scxml2.IDataExpandHelper.Expand.ifMeet
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.StrategyTripleHelper.StateRenEventSelector
import helper.scxml.scxml2.StrategyTripleHelper.StrategyTriple
import org.apache.commons.scxml2.SCXMLExecutor
import org.apache.commons.scxml2.model.Data
import org.apache.commons.scxml2.model.EnterableState

object EnvHelper {
    abstract class T1BaseEnv(
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
                return scxmlTuple.renStateList.contains(scxmlTuple.activeStatesString)
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

    abstract class T2BaseEnv(
        envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
        envEventLHM: A3LHM<String, String, Double>,
        getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
    ) : T1BaseEnv(
        envStateConstraintLHM,
        envEventLHM,
        getIRenEventSelectorFun,
    ){
        fun getEvent(
            debuggerList: DebugHelper.DebuggerList,
        ): String? {
            scxmlTuple.activeStates.map {
                it.id
            }.map { stateId ->
                if (isInFinalState()) return@map
                envStateConstraintLHM[stateId]!!.let {
                    if (!it.ifMeet(dataSCXML)) return@map
                    //dataXInt，这里不通用啊！！
                    //val dataInt=dataXInt
                    val dataInt = dataSCXML.getDataInt(scxmlTuple.stateNeedAddOneClockListLHM[stateId]!![0])!!
                    val booleanInProbability = RandomHelper.getBooleanInProbability(
                        it.maxV - dataInt + 1
                    )
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
            debuggerList: DebugHelper.DebuggerList,
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
            debuggerList: DebugHelper.DebuggerList,
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
            scxmlTuple.activeStateIds.map { stateId ->
                scxmlTuple.stateNeedAddOneClockListLHM[stateId]?.map {
                    dataSCXML.getData(it)?.setExprAddOne()
                }
            }
            dataGlobalTime.setExprAddOne()
            if (ifMachineTimeMax) {
                return
            }
        }

        fun stepToNeedRen(
            debuggerList: DebugHelper.DebuggerList,
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
            debuggerList: DebugHelper.DebuggerList = DebugHelper.getDebuggerList(0),
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
}