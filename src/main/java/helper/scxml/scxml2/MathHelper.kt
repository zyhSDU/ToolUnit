package helper.scxml.scxml2

import helper.base.BaseTypeHelper.ListExpand.toArrayList
import helper.base.DebugHelper.DebuggerList
import helper.base.DebugHelper.DebuggerList.Companion.getDebuggerList
import helper.base.LHMHelper.A3LHM
import helper.scxml.scxml2.MathHelper.ClockValuations.Expand.toArrayRealVector
import helper.scxml.scxml2.MathHelper.ClockValuations.Expand.toClockValuations
import helper.scxml.scxml2.MathHelper.ClockValuations.Expand.toDoubleArrayList
import helper.scxml.scxml2.MathHelper.ClockValuationsList.Expand.E2.toClockValuationsList
import helper.scxml.scxml2.MathHelper.ClockValuationsList.Expand.toArray2DRowRealMatrix
import helper.scxml.scxml2.MathHelper.ClockValuationsList.Expand.toDoubleArrayListArrayList
import helper.scxml.scxml2.MathHelper.Expand.calMean
import helper.scxml.scxml2.MathHelper.Expand.getCovarianceMatrix
import helper.scxml.scxml2.MathHelper.Expand.getEuclideanDistance
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.LUDecomposition
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

            fun ArrayList<Double>.toArrayRealVector(): ArrayRealVector {
                return ArrayRealVector(this.toDoubleArray())
            }

            fun ClockValuations.toDoubleArrayList(): ArrayList<Double> {
                val v = ArrayList<Double>()
                this.map {
                    v.add(it)
                }
                return v
            }

            fun ClockValuations.toArrayRealVector(): ArrayRealVector {
                return this.toDoubleArrayList().toArrayRealVector()
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

        private var privateCovarianceMatrix: ClockValuationsList? = null
            get() {
                if (field == null) {
                    field = this.toDoubleArrayListArrayList().getCovarianceMatrix().toClockValuationsList()
                }
                return field
            }
        val covarianceMatrix: ClockValuationsList
            get() {
                return privateCovarianceMatrix!!
            }

        fun getWeightOf(
            v: ClockValuations,
        ): Double {
            return size * Math.E.pow(-v.getEuclideanDistance(mean))
        }

        fun getDistanceToCovarianceMatrix(
            clockValuations: ClockValuations,
        ): Double {
            val u = clockValuations.toArrayRealVector()
            val v = mean.toArrayRealVector()
            val q = covarianceMatrix.toArray2DRowRealMatrix()

            // 计算矩阵 Q 的逆矩阵 Q^{-1}
            val luDecomposition = LUDecomposition(q)
            val qInverse = luDecomposition.solver.inverse

            // 计算 (u-\bar{v})^{T} Q^{-1}(u-\bar{v})
            val diff = u.subtract(v)
            val result = diff.dotProduct(qInverse.operate(diff))

            return result.pow(0.5)
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

            fun ClockValuationsList.toArray2DRowRealMatrix(): Array2DRowRealMatrix {
                val dss = ArrayList<DoubleArray>()

                this.map {
                    dss.add(it.toDoubleArray())
                }

                return Array2DRowRealMatrix(dss.toTypedArray())
            }

            fun ClockValuationsList.toDoubleArrayListArrayList(): ArrayList<ArrayList<Double>> {
                val dss = ArrayList<ArrayList<Double>>()
                this.map {
                    dss.add(it.toDoubleArrayList())
                }
                return dss
            }
        }
    }

    class LocationEventVListLHM : A3LHM<String, String, ClockValuationsList>()
}