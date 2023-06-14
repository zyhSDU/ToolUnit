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
                    maxIterations = 200,
                    maxResets = 10,
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
    ) {
        companion object {
            fun getObj1(): InstanceArgUnit {
                return InstanceArgUnit(
                    nowCountOfNoBetter = 0,
                    nowCountOfReset = 0,
                    heap = ArrayList(),
                    meanList = ArrayList(),
                    renEventSelectorCostListLHM = LinkedHashMap(),
                    renEventSelectorCostListLHMList = ArrayList(),
                )
            }
        }
    }
}