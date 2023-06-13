package helper.scxml.scxml2.t7_cycle

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
            fun getObj1() = HyperArgUnit(
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