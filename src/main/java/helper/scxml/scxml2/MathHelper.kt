package helper.scxml.scxml2

import helper.base.BaseTypeHelper.ListExpand.toArrayList
import helper.base.DebugHelper.DebuggerList
import helper.base.DebugHelper.DebuggerList.Companion.getDebuggerList
import helper.base.LHMHelper.A3LHM
import helper.scxml.scxml2.MathHelper.ClockValuations.Expand.toClockValuations
import helper.scxml.scxml2.MathHelper.Expand.calMean
import helper.scxml.scxml2.MathHelper.Expand.getEuclideanDistance
import kotlin.math.pow
import kotlin.math.sqrt

object MathHelper {
    object Expand {
        fun ArrayList<Double>.mathMinus(
            other: ArrayList<Double>,
        ): ArrayList<Double> {
            return this.zip(other) { a, b -> a - b }.toArrayList()
        }

        fun ArrayList<Double>.getEuclideanDistance(
            other: ArrayList<Double>,
        ): Double {
            val squaredDifferences = this.zip(other) { a, b -> (a - b).pow(2.0) }
            val sumOfSquaredDifferences = squaredDifferences.sum()
            return sqrt(sumOfSquaredDifferences)
        }

        fun ArrayList<out ArrayList<Double>>.calMean(): ArrayList<Double> {
            val mean = ArrayList<Double>()

            if (isEmpty()) {
                return mean
            }

            val size = size.toDouble()
            val columnSize = this[0].size

            for (i in 0 until columnSize) {
                val sum = sumOf { it[i] }
                mean.add(sum / size)
            }

            return mean
        }

        fun ArrayList<ArrayList<Double>>.getCovarianceMatrix(
            debuggerList: DebuggerList = getDebuggerList(0),
        ): ArrayList<ArrayList<Double>> {
            val covarianceMatrix = ArrayList<ArrayList<Double>>()

            if (isEmpty()) {
                return covarianceMatrix
            }

            val mean = calMean()
            debuggerList.pln("mean=\n${mean}\n")
            val sizeMinusOne = size.toDouble() - 1.0
            debuggerList.pln("sizeMinusOne=${sizeMinusOne}")
            val columnSize = this[0].size

            for (i in 0 until columnSize) {
                val row = ArrayList<Double>()
                for (j in 0 until columnSize) {
                    var sum = 0.0
                    for (k in this.indices) {
                        debuggerList.pln("k=${k}")
                        val qi = this[k][i] - mean[i]
                        val qj = this[k][j] - mean[j]
                        sum += qi * qj
                        debuggerList.pln("qi=${qi},qj=${qj},qi * qj=${qi * qj}")
                    }
                    debuggerList.pln("sum=${sum}")
                    val qij = sum / sizeMinusOne
                    debuggerList.pln("i=${i},j=${j},${qij}")
                    row.add(qij)
                }
                covarianceMatrix.add(row)
            }

            return covarianceMatrix
        }
    }

    class ClockValuations : ArrayList<Double>() {
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
        private var privateMean: ClockValuations? = null
            get() {
                if (field == null) {
                    field = this.calMean().toClockValuations()
                }
                return field
            }

        val mean: ClockValuations
            get() {
                return privateMean!!
            }

        fun getWeightOf(
            v: ClockValuations,
        ): Double {
            return size * Math.E.pow(-v.getEuclideanDistance(calMean()))
        }

        fun getCovarianceMatrix() {

        }

        object Expand {
            object E1 {
                fun ArrayList<ClockValuations>.toClockValuationsList(): ClockValuationsList {
                    val v = ClockValuationsList()
                    this.map {
                        v.add(it)
                    }
                    return v
                }
            }

            object E2 {
                fun ArrayList<ArrayList<Double>>.toClockValuationsList(): ClockValuationsList {
                    val v = ClockValuationsList()
                    this.map {
                        v.add(it.toClockValuations())
                    }
                    return v
                }
            }
        }
    }

    class LocationEventVListLHM : A3LHM<String, String, ClockValuationsList>() {

    }
}