package helper.scxml.scxml2.t7_cycle

import helper.base.BaseTypeHelper.ListExpand.toArrayList
import helper.base.DebugHelper
import helper.base.DebugHelper.DebuggerList
import helper.base.DebugHelper.getDebuggerList
import helper.base.LHMHelper.LHMExpand.add
import helper.base.LHMHelper.LHMExpand.addList
import helper.scxml.scxml2.EnvHelper.RunResult
import helper.scxml.scxml2.EnvHelper.T3BaseEnv
import helper.scxml.scxml2.MathHelper.ClockValuations
import helper.scxml.scxml2.MathHelper.ClockValuationsList
import helper.scxml.scxml2.MathHelper.LocationEventVListLHM
import helper.scxml.scxml2.Res
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.scxml2.StrategyTripleHelper
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.StrategyTripleHelper.Type2StrategyTuple
import helper.scxml.scxml2.t7_cycle.EnvHelper.Expand.toCostList

object EnvHelper {
    object Expand {
        fun LinkedHashMap<String, String>.toClockValuations(
        ): ClockValuations {
            val v = ClockValuations()
            v.add(this[Res.globalTimeId]!!.toDouble())
            v.add(this["x"]!!.toDouble())
            return v
        }

        fun ArrayList<RunResult>.toLocationEventVListLHM(
        ): LocationEventVListLHM {
            val lhm = LocationEventVListLHM()
            this.map {
                it.us.map {
                    lhm.add(it.location, it.action, ClockValuationsList())
                    lhm[it.location]!![it.action]!!.add(it.data.toClockValuations())
                }
            }
            return lhm
        }

        fun ArrayList<RunResult>.toCostList(): ArrayList<Double> {
            return this.map {
                it.endData["c"]!!.toInt().toDouble()
            }.toArrayList()
        }

        fun ArrayList<RunResult>.toMeanCost(): Double {
            return this.toCostList().average()
        }
    }

    class Env(
        override val strategyTuple: Type2StrategyTuple,
        private val machineTimeMax: Int,
    ) : T3BaseEnv() {
        override val scxmlTuple = Scxml2Helper.getSCXMLTuple("scxml2/t7_cycle/cycle1.scxml").also {
            it.initialStateList.add("s0")
            it.renStateList.add("s1")
            it.finalStateList.add("s4")
            arrayListOf("s0", "s1", "s2", "s3").map { stateId ->
                it.stateNeedConsiderClockListLHM.add(stateId, arrayListOf("x"))
            }
            arrayListOf("s0", "s1", "s2", "s3").map { stateId ->
                it.stateDataIncrementLHM.add(stateId, "x", 1.0)
            }
            it.stateDataIncrementLHM.run {
                add("s0", "x", 1.0)
                add("s1", "c", 4.0)
                add("s2", "c", 3.0)
                add("s3", "c", 2.0)
            }
        }

        override val ifMachineTimeMax: Boolean
            get() {
                return dataGlobalTimeInt >= machineTimeMax
            }

        fun repeatRun2(
            times: Int,
            runResultList: ArrayList<RunResult> = ArrayList(),
            debuggerList: DebuggerList = getDebuggerList(0),
        ) {
            repeat(times) {
                this.reset()
                this.taskRun2(
                    debuggerList = debuggerList
                ).let {
                    debuggerList.pln(
                        it.toStr(0),
                        arrayListOf(0, 1, 2),
                    )
                    runResultList.add(it)
                }
            }
        }

        fun repeatRun2AndRecord(
            times: Int,
            lhm: LinkedHashMap<(SCXMLTuple) -> IRenEventSelector, ArrayList<Double>>,
            runResultList: ArrayList<RunResult> = ArrayList(),
            debuggerList: DebuggerList = getDebuggerList(0),
        ) {
            repeatRun2(
                times,
                runResultList,
                debuggerList,
            )
            lhm.addList(
                this.strategyTuple.getRenEventSelectorFun,
                runResultList.toCostList(),
            )
        }
    }
}
