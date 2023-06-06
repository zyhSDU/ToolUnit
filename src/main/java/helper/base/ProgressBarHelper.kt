package helper.base

object ProgressBarHelper {
    fun printProgressBar(progress: Int, total: Int, prefix: String = "Progress:", suffix: String = "Complete") {
        val percent = String.format("%.2f", (progress.toDouble() / total.toDouble()) * 100)
        val progressBarWidth = 50
        val progressBar = StringBuilder()
        val progressFill = (progress.toDouble() / total.toDouble() * progressBarWidth).toInt()

        progressBar.append("[")
        for (i in 0 until progressBarWidth) {
            if (i < progressFill) {
                progressBar.append("=")
            } else {
                progressBar.append(" ")
            }
        }
        progressBar.append("] $percent%")

        val paddingLength = progressBarWidth - progressBar.length
        if (paddingLength > 0) {
            progressBar.append(" ".repeat(paddingLength))
        }

        print("\r$prefix $progressBar $suffix")
        if (progress == total) {
            println()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        for (i in 1..100) {
            printProgressBar(i, 100)
            Thread.sleep(100)
        }
    }
}