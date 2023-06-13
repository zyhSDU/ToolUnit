package helper.scxml.scxml2.t5_traffic_train.math

import helper.base.DebugHelper
import helper.base.DebugHelper.getDebuggerList
import helper.scxml.scxml2.MathHelper.ClockValuations.Expand.toClockValuations
import helper.scxml.scxml2.MathHelper.ClockValuationsList.Expand.E1.toClockValuationsList
import helper.scxml.scxml2.MathHelper.ClockValuationsList.Expand.E2.toClockValuationsList
import helper.scxml.scxml2.MathHelper.Expand.getCovarianceMatrix
import helper.scxml.scxml2.MathHelper.Expand.getEuclideanDistance
import helper.scxml.scxml2.MathHelper.Expand.mathMinus
import org.junit.Test
import kotlin.math.pow

internal class Test {
    @Test
    fun t0() {
        val d = 3.0 * Math.E.pow(-(2.0).pow(0.5))
        val rs = d.toString()
        println(rs)
        assert(
            rs == """
            0.7293502033026426
        """.trimIndent()
        )
    }

    @Test
    fun t1() {
        val v1 = arrayListOf(11.0, 1.0).toClockValuations()
        val v2 = arrayListOf(12.0, 2.0).toClockValuations()
        val v3 = arrayListOf(13.0, 3.0).toClockValuations()
        val vs = arrayListOf(v1, v2, v3).toClockValuationsList()
        println(vs)
        val mean = vs.mean
        println(mean)
        vs.map {
            val minus = it.mathMinus(mean)
            println(minus)
        }
        vs.map {
            val weightOf = vs.getWeightOf(it)
            println(weightOf)
        }
        //[[11.0, 1.0], [12.0, 2.0], [13.0, 3.0]]
        //[12.0, 2.0]
        //[-1.0, -1.0]
        //[0.0, 0.0]
        //[1.0, 1.0]
        //0.7293502033026426
        //3.0
        //0.7293502033026426
    }

    @Test
    fun t2() {
        val v1 = arrayListOf(3.0, 4.0).toClockValuations()
        val v2 = arrayListOf(6.0, 8.0).toClockValuations()
        val euclideanDistance = v1.getEuclideanDistance(v2)
        val rs = euclideanDistance.toString()
        println(euclideanDistance)
        assert(
            rs == """
            5
        """.trimIndent()
        )
    }

    @Test
    fun t3() {
        val v1 = arrayListOf(1.0, 2.0, 3.0).toClockValuations()
        val v2 = arrayListOf(4.0, 5.0, 6.0).toClockValuations()
        val euclideanDistance = v1.getEuclideanDistance(v2)
        val rs = euclideanDistance.toString()
        println(euclideanDistance)
        assert(
            rs == """
            5.196152422706632
        """.trimIndent()
        )
    }

    @Test
    fun testMean() {
        val list = arrayListOf(
            arrayListOf(1.0, 2.0, 3.0),
            arrayListOf(4.0, 5.0, 6.0),
            arrayListOf(7.0, 8.0, 9.0)
        ).toClockValuationsList()

        println(list.mean)
    }

    @Test
    fun test_getCovarianceMatrix() {
        val list = arrayListOf(
            arrayListOf(1.0, 2.0, 3.0),
            arrayListOf(4.0, 5.0, 6.0),
            arrayListOf(7.0, 8.0, 9.0)
        )

        val covarianceMatrix = list.getCovarianceMatrix()
        covarianceMatrix.forEach { row ->
            println(row)
        }
    }

    @Test
    fun test2_getCovarianceMatrix() {
        val list = arrayListOf(
            arrayListOf(3.0, 1.0, 8.0),
            arrayListOf(2.0, 6.0, 7.0),
            arrayListOf(4.0, 9.0, 5.0),
            arrayListOf(5.0, 4.0, 2.0),
        )

        val covarianceMatrix = list.getCovarianceMatrix(
            getDebuggerList(0)
        )
        covarianceMatrix.forEach { row ->
            println(row)
        }
    }
}