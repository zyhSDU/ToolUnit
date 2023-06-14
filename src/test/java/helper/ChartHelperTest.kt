package helper

import helper.ChartHelper.toXYData
import helper.base.BaseTypeHelper.ListExpand.toArrayList
import helper.base.TimeHelper
import org.junit.Test
import res.FileRes.out_chart_file

internal class ChartHelperTest {
    @Test
    fun testXYData() {
        val ds = (10..20).map { it.toDouble() }.toArrayList()
        val toXYData = ds.toXYData()
        toXYData.map {
            it.map {
                @Suppress("NestedLambdaShadowedImplicitParameter")
                print("${it},\t")
            }
            println()
        }
    }

    @Test
    fun taskPoissonDistributionChart() {
        val nowTimeStr = TimeHelper.now(TimeHelper.TimePattern.p4)
        (1..10).map {
            ChartHelper.taskPoissonDistributionChart(
                it.toDouble(),
                30,
                "$out_chart_file" +
                        "/taskPoissonDistributionChart" +
                        "/t_${nowTimeStr}" +
                        "/chart${it}.png"
            )
        }
    }
}