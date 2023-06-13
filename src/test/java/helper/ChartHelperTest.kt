package helper

import helper.base.TimeHelper
import org.junit.Test
import res.FileRes.out_chart_file

internal class ChartHelperTest {
    @Test
    fun t_2023_0613_221615_() {
        val nowTimeStr=TimeHelper.now(TimeHelper.TimePattern.p4)
        (1..10).map {
            ChartHelper.getPoissonDistributionPlot(
                it.toDouble(),
                30,
                "$out_chart_file" +
                        "/t_2023_0613_221615_" +
                        "/t_${nowTimeStr}" +
                        "/chart${it}.png"
            )
        }
    }
}