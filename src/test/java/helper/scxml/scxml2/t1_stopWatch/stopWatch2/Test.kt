package helper.scxml.scxml2.t1_stopWatch.stopWatch2

import helper.base.PrintHelper.StringTo.toPrintln
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.scxml2.t1_stopWatch.StopWatchEntity

object Test {
    @JvmStatic
    fun main(args: Array<String>) {
        val scxmlTuple = Scxml2Helper.getSCXMLTuple("scxml2/stopwatch1.scxml")

        //实例化数据模型解析器
        val evaluator = scxmlTuple.evaluator

        //实例化引擎
        val executor = scxmlTuple.executor

        //设置引擎执行的根上下文
        val rootContext = evaluator.newContext(null)
        val stopWatchEntity = StopWatchEntity()
        rootContext["stopWatchEntity"] = stopWatchEntity
        executor.rootContext = rootContext

        val eventNames = arrayOf(
            "",
            "watch.start",
            "watch.stop",
            "watch.reset",
        )

        val eventIndexes = arrayListOf(0, 1, 0, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 2, 0, 3, 0)

        fun infoPrintln() {
            scxmlTuple.statusPrintln()
            stopWatchEntity.display.toPrintln()
        }

        //开始启动流程
        executor.go()

        eventIndexes.map {
            infoPrintln()
            Thread.sleep(1000)
            scxmlTuple.fireEvent(eventNames[it])
        }
    }
}