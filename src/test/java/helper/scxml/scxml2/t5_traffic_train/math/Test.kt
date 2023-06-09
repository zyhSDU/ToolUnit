package helper.scxml.scxml2.t5_traffic_train.math

import helper.scxml.scxml2.MathHelper.ClockValuations.Expand.toClockValuations
import helper.scxml.scxml2.MathHelper.ClockValuationsList.Expand.toClockValuationsList
import org.junit.Test
import kotlin.math.pow

internal class Test {
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