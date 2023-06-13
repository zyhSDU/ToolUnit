package helper.scxml.scxml2.t7_cycle

import helper.base.LHMHelper.LHMExpand.add
import helper.scxml.scxml2.EnvHelper
import helper.scxml.scxml2.EnvHelper.RunResult
import helper.scxml.scxml2.MathHelper.ClockValuations
import helper.scxml.scxml2.MathHelper.ClockValuationsList
import helper.scxml.scxml2.MathHelper.LocationEventVListLHM
import helper.scxml.scxml2.Res
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.scxml2.StrategyTripleHelper

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

        fun ArrayList<RunResult>.toMeanCost(): Double {
            return this.map {
                it.endData["c"]!!.toInt()
            }.average()
        }
    }

    class Env(
        override val strategyTuple: StrategyTripleHelper.Type2StrategyTuple,
        private val machineTimeMax: Int,
    ) : EnvHelper.T3BaseEnv() {
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
    }
}
