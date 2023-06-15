package helper.scxml.scxml2.t7_cycle

import helper.scxml.scxml2.EnvHelper.RunResult
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector

object LearningHelper {
    class HyperArgUnit(
        val maxRuns: Int = 0,
        val maxGood: Int = 0,
        val maxBest: Int = 0,
        val evalRuns: Int = 0,
        val maxNoBetter: Int = 0,
        val maxIterations: Int = 0,
        val maxResets: Int = 0,
    ) {
        companion object {
            fun getObj1(): HyperArgUnit {
                return HyperArgUnit(
                    maxRuns = 100,
                    maxGood = 10,
                    maxBest = 100,
                    evalRuns = 10,
                    maxNoBetter = 10,
                    maxIterations = Int.MAX_VALUE,
                    maxResets = 20,
                )
            }

            fun getObj2(): HyperArgUnit {
                return HyperArgUnit(
                    maxRuns = 2,
                    maxGood = 2,
                    maxBest = 2,
                    evalRuns = 2,
                    maxNoBetter = 2,
                    maxIterations = 2,
                    maxResets = 2,
                )
            }
        }
    }

    class InstanceArgUnit(
        var nowCountOfNoBetter: Int = 0,
        var nowCountOfReset: Int = 0,
        var heap: ArrayList<RunResult> = ArrayList(),
        val meanList: ArrayList<Double> = ArrayList(),
        var renEventSelectorCostListLHM: LinkedHashMap<(SCXMLTuple) -> IRenEventSelector, ArrayList<Double>> = LinkedHashMap(),
        val renEventSelectorCostListLHMList: ArrayList<LinkedHashMap<(SCXMLTuple) -> IRenEventSelector, ArrayList<Double>>> = ArrayList(),
        var renEventSelectorCostMean2LHM: LinkedHashMap<(SCXMLTuple) -> IRenEventSelector, Double> = LinkedHashMap(),
        val renEventSelectorCostMean2LHMList: ArrayList<LinkedHashMap<(SCXMLTuple) -> IRenEventSelector, Double>> = ArrayList(),
        var lastMinCost: Double = Double.MAX_VALUE,
        var indexAfterReset: Int = 0,
    ) {
        companion object {
            fun getObj1(): InstanceArgUnit {
                return InstanceArgUnit()
            }
        }

        init {
            renEventSelectorCostListLHMList.add(renEventSelectorCostListLHM)
            renEventSelectorCostMean2LHMList.add(renEventSelectorCostMean2LHM)
        }

        fun resetLastMinCost() {
            lastMinCost = Double.MAX_VALUE
        }
    }
}