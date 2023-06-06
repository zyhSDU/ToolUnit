package helper.base

import kotlin.math.max
import kotlin.random.Random

object MathHelper {
    fun Int.modifiedSubtract(int: Int): Int {
        return max(0, this - int)
    }

    fun getRandomString(
        stringDistributionProbabilityMap: LinkedHashMap<String, Double>,
    ): String? {
        val random = Random.nextDouble() * stringDistributionProbabilityMap.values.sum()
        var cumulativeProbability = 0.0

        for ((string, prob) in stringDistributionProbabilityMap) {
            cumulativeProbability += prob
            if (random <= cumulativeProbability) {
                return string
            }
        }
        return null
    }

    //可以放弃
    fun getRandomStringWithLeftTime(
        stringDistributionProbabilityMap: Map<String, Double>,
        leftTime: Int = 1,
    ): String? {
        val random = Random.nextDouble() * stringDistributionProbabilityMap.values.sum() * leftTime
        var cumulativeProbability = 0.0

        for ((string, prob) in stringDistributionProbabilityMap) {
            cumulativeProbability += prob
            if (random <= cumulativeProbability) {
                return string
            }
        }
        return null
    }
}