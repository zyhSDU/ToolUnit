package helper.scxml.scxml2

import org.junit.Test
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.LUDecomposition
import kotlin.math.pow

internal class MathHelperTest {
    @Test
    fun t1() {
        // 定义向量 u 和向量 v
        val u = ArrayRealVector(doubleArrayOf(1.0, 2.0, 3.0))
        val v = ArrayRealVector(doubleArrayOf(4.0, 5.0, 6.0))

        val arrayOf: Array<DoubleArray> = arrayOf(
            doubleArrayOf(1.0, 0.0, 0.0),
            doubleArrayOf(0.0, 2.0, 0.0),
            doubleArrayOf(0.0, 0.0, 3.0)
        )
        // 定义矩阵 Q
        val q = Array2DRowRealMatrix(arrayOf)

        // 计算矩阵 Q 的逆矩阵 Q^{-1}
        val luDecomposition = LUDecomposition(q)
        val qInverse = luDecomposition.solver.inverse

        // 计算 (u-\bar{v})^{T} Q^{-1}(u-\bar{v})
        val diff = u.subtract(v)
        println(diff)
        val result: Double = diff.dotProduct(qInverse.operate(diff))

        println(result) // 输出结果
        println(result.pow(0.5))
    }
}