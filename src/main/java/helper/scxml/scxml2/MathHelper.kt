package helper.scxml.scxml2

import helper.base.LHMHelper.A3LHM
import kotlin.math.pow

object MathHelper {
    class ClockValuations : ArrayList<Double>() {
        fun minus(
            v2: ClockValuations,
        ): ClockValuations {
            val v1 = this
            val v3 = ClockValuations()
            (0 until v1.size).map {
                v3.add(v1[it] - v2[it])
            }
            return v3
        }

        fun getEuclideanDistance(
            v2: ClockValuations,
        ): Double {
            val v1 = this
            var res = 0.0
            (0 until v1.size).map {
                res += (v1[it] - v2[it]).pow(2.0)
            }
            res = res.pow(0.5)
            return res
        }

        object Expand {
            fun ArrayList<Double>.toClockValuations(): ClockValuations {
                val v = ClockValuations()
                this.map {
                    v.add(it)
                }
                return v
            }
        }
    }

    class ClockValuationsList : ArrayList<ClockValuations>() {
        private var mean: ClockValuations? = null

        fun calMean(): ClockValuations {
            if (mean != null) return mean!!
            val v = ClockValuations()
            (0 until this[0].size).map { i ->
                var d = 0.0
                this.map { j ->
                    d += j[i]
                }
                d /= size
                v.add(d)
            }
            mean = v
            return v
        }

        fun getWeightOf(v: ClockValuations): Double {
            return size * Math.E.pow(-v.getEuclideanDistance(calMean()))
        }

        fun getCovarianceMatrix() {

        }

        object Expand {
            fun ArrayList<ClockValuations>.toClockValuationsList(): ClockValuationsList {
                val v = ClockValuationsList()
                this.map {
                    v.add(it)
                }
                return v
            }
        }
    }

    class LocationEventVListLHM : A3LHM<String, String, ClockValuationsList>() {

    }
}