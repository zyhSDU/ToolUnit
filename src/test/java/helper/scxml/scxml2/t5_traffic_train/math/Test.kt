package helper.scxml.scxml2.t5_traffic_train.math

import helper.scxml.scxml2.t5_traffic_train.math.Test.ClockValuations.Expand.toClockValuations
import helper.scxml.scxml2.t5_traffic_train.math.Test.ClockValuationsList.Expand.toClockValuationsList
import org.junit.Test
import kotlin.math.pow

internal class Test {
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

    @Test
    fun t0() {
        println(3.0 * Math.E.pow(-(2.0).pow(0.5)))
    }

    @Test
    fun t1() {
        val v1 = arrayListOf(11.0, 1.0).toClockValuations()
        val v2 = arrayListOf(12.0, 2.0).toClockValuations()
        val v3 = arrayListOf(13.0, 3.0).toClockValuations()
        val vs = arrayListOf(v1, v2, v3).toClockValuationsList()
        println(vs)
        val mean = vs.calMean()
        println(mean)
        vs.map {
            val minus = it.minus(mean)
            println(minus)
        }
        vs.map {
            val weightOf = vs.getWeightOf(it)
            println(weightOf)
        }
    }

    @Test
    fun t2() {
        val v1 = arrayListOf(3.0, 4.0).toClockValuations()
        val v2 = arrayListOf(6.0, 8.0).toClockValuations()
        val euclideanDistance = v1.getEuclideanDistance(v2)
        println(euclideanDistance)
    }

    @Test
    fun t3() {
        val v1 = arrayListOf(1.0, 2.0, 3.0).toClockValuations()
        val v2 = arrayListOf(4.0, 5.0, 6.0).toClockValuations()
        val euclideanDistance = v1.getEuclideanDistance(v2)
        println(euclideanDistance)
    }
}