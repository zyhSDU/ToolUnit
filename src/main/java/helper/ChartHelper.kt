package helper

import org.apache.commons.math3.distribution.PoissonDistribution
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.DefaultXYDataset
import java.io.File
import javax.swing.JFrame

object ChartHelper {
    fun ArrayList<Double>.toXYData(): Array<DoubleArray> {
        return arrayOf(
            this.indices.map { it.toDouble() }.toDoubleArray(),
            this.toDoubleArray(),
        )
    }

    fun taskChart(
        chart: JFreeChart,
        saveFile: String? = null,
        ifShow: Boolean = false,
        frameTitle: String = "",
    ) {
        if (saveFile != null) {
            // 保存图表为 PNG 格式的图片文件
            val outputFile = File(saveFile)
            outputFile.parentFile.mkdirs()
            ChartUtils.saveChartAsPNG(outputFile, chart, 500, 400)
            println("Chart saved as ${outputFile.absolutePath}")
        }

        if (ifShow) {
            // 创建图表面板
            val chartPanel = ChartPanel(chart)
            // 创建窗口并显示图表面板
            val frame = JFrame(frameTitle)
            frame.contentPane = chartPanel
            frame.pack()
            frame.isVisible = true
        }
    }

    fun drawLineChart(
        yData: ArrayList<Double>,
    ): JFreeChart {
        // 创建数据集
        val dataset = DefaultXYDataset()
        dataset.addSeries("Series 1", yData.toXYData())

        // 创建图表并设置样式
        return ChartFactory.createXYLineChart(
            "lineChart",
            "x",
            "y",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )
    }

    fun taskDrawLineChart(
        yData: ArrayList<Double>,
        saveFile: String? = null,
        ifShow: Boolean = false,
    ) {
        val chart: JFreeChart = drawLineChart(yData)
        taskChart(
            chart,
            saveFile,
            ifShow,
            "drawLineChart",
        )
    }

    fun getPoissonDistributionChart(
        xMax: Int,
        lambda: Double
    ): JFreeChart {
        // 创建一个包含整数 0~10 的数组
        val x: IntArray = IntArray(xMax) { it }

        // 计算泊松分布的概率质量函数
        val pmf: DoubleArray = DoubleArray(xMax) {
            PoissonDistribution(lambda).probability(it)
        }

        // 创建数据集
        val dataset = DefaultCategoryDataset()
        for (i in x.indices) {
            dataset.addValue(pmf[i], "Poisson PMF", x[i])
        }

        // 创建图表并设置样式
        val chart: JFreeChart = ChartFactory.createBarChart(
            "Poisson PMF with λ=$lambda",
            "Number of Events",
            "Probability",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )
        return chart
    }

    fun taskPoissonDistributionChart(
        // 设置参数 lambda
        lambda: Double,
        // 设置横坐标最大值
        xMax: Int,
        saveFile: String? = null,
        ifShow: Boolean = false,
    ) {
        val chart: JFreeChart = getPoissonDistributionChart(xMax, lambda)
        taskChart(
            chart,
            saveFile,
            ifShow,
            "Poisson PMF",
        )
    }
}