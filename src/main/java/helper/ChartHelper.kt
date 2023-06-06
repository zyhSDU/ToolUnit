package helper

import org.apache.commons.math3.distribution.PoissonDistribution
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import javax.swing.JFrame

object ChartHelper {
    fun getPoissonDistributionPlot(
        // 设置参数 lambda
        lambda: Double,
        // 设置横坐标最大值
        xMax: Int,
    ) {
        // 创建一个包含整数 0~10 的数组
        val x = IntArray(xMax) { it }

        // 计算泊松分布的概率质量函数
        val pmf = DoubleArray(xMax) { PoissonDistribution(lambda).probability(it) }

        // 创建数据集
        val dataset = DefaultCategoryDataset()
        for (i in x.indices) {
            dataset.addValue(pmf[i], "Poisson PMF", x[i])
        }

        // 创建图表并设置样式
        val chart = ChartFactory.createBarChart(
            "Poisson PMF with λ=$lambda",
            "Number of Events",
            "Probability",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        // 创建图表面板
        val chartPanel = ChartPanel(chart)

        // 创建窗口并显示图表面板
        val frame = JFrame("Poisson PMF")
        frame.contentPane = chartPanel
        frame.pack()
        frame.isVisible = true
    }

    @JvmStatic
    fun main(args: Array<String>) {
        (1..10).map {
            getPoissonDistributionPlot(it.toDouble(), 30)
        }
    }
}