package helper.scxml.scxml2.t2_traffic.fun_strategy

import helper.base.DebugHelper.DebuggerList
import helper.base.LHMHelper.A4LHM
import helper.base.MathHelper
import helper.base.RandomHelper
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml0.TrafficTestHelper
import helper.scxml.scxml2.Expand.SCXMLExecutorExpand.isInState
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.t2_traffic.Res
import org.apache.commons.scxml2.model.Data
import org.apache.commons.scxml2.model.TransitionTarget
import java.io.File

object FunStrategyHelper {
    data class SIISUnit(
        val stateId: String,
        val gTime: Int,
        val retry: Int,
        val event: String,
    )

    /*
   状态
   已经重试次数retry
   选取的event
   统计价值,剩余时间
   具体运行时，需要记录每个状态的上次被进入的全局时间
   */
    class StrIntStrIntLHM : A4LHM<String, Int, String, Int>() {
        fun writeToFile(file: File) {
            this.touch { a1, a2, a3, a4 ->
                file.appendText("${a1},${a2},${a3},${a4}\n")
            }
        }

        fun addLeftTime(it: SIISUnit, leftTime: Int) {
            this[it.stateId]!![it.retry]!![it.event] =
                this[it.stateId]!![it.retry]!![it.event]!! + leftTime
        }

        companion object {
            fun getInitialLHM(): StrIntStrIntLHM {
                val a4LHM = StrIntStrIntLHM()
                arrayOf("Aalborg", "Wait").map { i1 ->
                    (0..6).map { i2 ->
                        when (i1) {
                            "Aalborg" -> {
                                arrayOf("car", "bike", "train").map { i3 ->
                                    a4LHM.add(i1, i2, i3, 0)
                                }
                            }
                            "Wait" -> {
                                arrayOf("train_wait_train", "train_wait_back").map { i3 ->
                                    a4LHM.add(i1, i2, i3, 0)
                                }
                            }
                            else -> {
                            }
                        }
                    }
                }
                return a4LHM
            }

            fun getLHMFromFile(fileString: String): StrIntStrIntLHM {
                val a4LHM = StrIntStrIntLHM()
                File(fileString).readLines().map {
                    val split = it.split(",")
                    a4LHM.add(split[0], split[1].toInt(), split[2], split[3].toInt())
                }
                return a4LHM
            }
        }
    }

    //stateEnvConstraintLHM
    //envEventLHM
    //GetRenEventUnitInterface
    fun getEvent(
        scxmlTuple: SCXMLTuple,
        envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
        envEventLHM: LinkedHashMap<String, LinkedHashMap<String, Double>>,
        renEventSelector: IRenEventSelector,
        firedStateIdEventList: ArrayList<SIISUnit>,
        stateEnterTimeLHM: LinkedHashMap<String, Int>,
        debuggerList: DebuggerList,
    ): String? {
        val mySCXML = scxmlTuple.dataSCXML
        val executor = scxmlTuple.executor
        val dataT: Int = mySCXML.getDataInt("T")!!
        executor.status.activeStates.map {
            it.id
        }.map { stateId ->
            if (stateId == "Sydney") return@map
            envStateConstraintLHM[stateId]!!.let {
                if (!it.ifMeet(dataT)) return@map
                val booleanInProbability = RandomHelper.getBooleanInProbability(it.maxV - dataT + 1)
                if (!booleanInProbability) return@map
            }
            if (envEventLHM.containsKey(stateId)) {
                envEventLHM[stateId]?.let {
                    MathHelper.getRandomString(it)
                }?.let {
                    return it
                }
            }
            renEventSelector.getEvent(stateId)?.let { event ->
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

    fun taskRun(
        outA4LHM: StrIntStrIntLHM?,
        envStateConstraintLHM: LinkedHashMap<String, ClockConstraint>,
        envEventLHM: LinkedHashMap<String, LinkedHashMap<String, Double>>,
        getIRenEventSelectorFun: (SCXMLTuple) -> IRenEventSelector,
        debuggerList: DebuggerList,
    ): Int {
        debuggerList.pln("--taskRun${"-".repeat(80)}")
        val globalTimeId = helper.scxml.scxml2.Res.globalTimeId
        val scxmlTuple = TrafficTestHelper.getSCXMLTuple()
        val executor = scxmlTuple.executor
        val mySCXML = scxmlTuple.dataSCXML

        val idDataLHM = LinkedHashMap<String, Data>()
        arrayOf("T", "retryTrainCount").map {
            idDataLHM[it] = mySCXML.getData(it)!!
        }

        //具体运行时，需要记录每个状态的上次被进入的全局时间
        val stateEnterTimeLHM = LinkedHashMap<String, Int>()
        stateEnterTimeLHM["Aalborg"] = 0

        val doOnEntry = { it: TransitionTarget ->
            stateEnterTimeLHM[it.id] = mySCXML.getDataInt(globalTimeId)!!
        }

        val firedStateIdEventList = ArrayList<SIISUnit>()

        fun strategyFireEvent() {
            val event = getEvent(
                scxmlTuple = scxmlTuple,
                envStateConstraintLHM = envStateConstraintLHM,
                envEventLHM = envEventLHM,
                renEventSelector = getIRenEventSelectorFun(scxmlTuple),
                firedStateIdEventList = firedStateIdEventList,
                stateEnterTimeLHM = stateEnterTimeLHM,
                debuggerList = debuggerList,
            )
            if (event != null) {
                scxmlTuple.fireEvent(
                    event = event,
                    doOnEntryFun = doOnEntry,
                    debuggerList = debuggerList,
                )
                strategyFireEvent()
            }
        }

        executor.go()

        val value: Int
        if (debuggerList.arr[0].ifDebug) scxmlTuple.statusPrintln()
        while (true) {
            strategyFireEvent()
            if (executor.isInState("Sydney")) {
                val leftTime = 60 - mySCXML.getDataInt(globalTimeId)!!
                value = leftTime
                if (outA4LHM == null) break
                firedStateIdEventList.map {
                    debuggerList.pln(it.toString())
                    outA4LHM.addLeftTime(it, leftTime)
                }
                break
            }
            if (mySCXML.ifDataExprEqualInt(globalTimeId, 60)) {
                val leftTime = -60 * 1000000
                value = leftTime
                if (outA4LHM == null) break
                firedStateIdEventList.map {
                    debuggerList.pln(it.toString())
                    outA4LHM.addLeftTime(it, leftTime)
                }
                break
            }
            mySCXML.setDataExprAddOne("T")
            mySCXML.setDataExprAddOne(globalTimeId)
        }
        if (debuggerList.arr[0].ifDebug) scxmlTuple.statusPrintln()
        return value
    }
}