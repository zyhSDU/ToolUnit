package helper

import helper.base.FileHelper
import helper.base.TimeHelper
import java.lang.StringBuilder
import java.io.File as File

object LogHelper {
    enum class LogLabel {
        traceNull,  //traceNull,占位,勿用
        trace0,     //trace0,跟踪所有
        trace1,
        debug1,
    }

    enum class LogLabelGroup(var arrayList: ArrayList<LogLabel>) {
        all(ArrayList<LogLabel>().apply {
            addAll(LogLabel.values())
        }),
        now(
            arrayListOf(
                LogLabel.traceNull, //traceNull,占位,勿删
//                LogLabel.trace0,
                LogLabel.trace1,
            )
        ),
        trace0(createLogLabelsWithTrace0()),
        trace1(createLogLabelsWithTrace0(LogLabel.trace1)),
        debug1(createLogLabelsWithTrace0(LogLabel.debug1)),
        ;

        fun contains(logLabel: LogLabel): Boolean {
            return arrayList.contains(logLabel)
        }
    }

    fun createLogLabelsWithTrace0(vararg label: LogLabel): ArrayList<LogLabel> {
        return arrayListOf(LogLabel.trace0).apply {
            addAll(label)
        }
    }

    enum class LogLevel {
        trace,
        debug,
        warn,
        error,
        fatal
    }

    enum class LogOutEnum(val run: (String) -> Unit) {
        print({ s ->
            println(s)
        }),
        text({ s ->
            val file = File("log\\${TimeHelper.now()}.txt")
            FileHelper.createFileIfNotExists(file)
            file.appendText(s)
            file.appendText("\n")
        }),
        ;
    }

    enum class LogOutGroupEnum(val arr: Array<LogOutEnum>) {
        none(arrayOf()),
        print(arrayOf(LogOutEnum.print)),
        all(LogOutEnum.values())
    }

    //修改这个可以修改log输出级别
    val logOutGroupEnum = LogOutGroupEnum.all

    class LogEntity(
        var logLevel: LogLevel = LogLevel.debug,
        val logMessage: String = "",
        val logLabelGroup: LogLabelGroup,
        val isSimplify: Boolean = true
    ) {
        override fun toString(): String {
            return ArrayList<String>().apply {
                add("$logLevel")
                add(TimeHelper.now())
                logLabelGroup.arrayList.map {
                    add(it.name)
                }
            }.run arrayList@{
                val stringBuilder = StringBuilder()
                if (!isSimplify) {
                    stringBuilder.append("-".repeat(40))
                    this.map {
                        stringBuilder.append("[${it}]")
                    }
                }
                stringBuilder
            }.apply {
                if (logMessage.isNotBlank()) {
                    if (!isSimplify) append("\n")
                    append(logMessage)
                }
            }.run {
                toString()
            }
        }
    }

    //log
    private fun log(logEntity: LogEntity) {
        var contains = false
        logEntity.logLabelGroup.arrayList.map {
            if (!contains) {
                if (LogLabelGroup.now.contains(it)) {
                    contains = true
                }
            }
        }
        if (!contains) return
        logEntity.toString().run {
            logOutGroupEnum.arr.map {
                it.run(this)
            }
            MessageHelper.mLogUtil.send(MessageHelper.Message(this))
        }
    }

    private fun log(
        logLevel: LogLevel = LogLevel.trace,
        logMessage: String = "",
        logLabelGroup: LogLabelGroup = LogLabelGroup.trace0,
        isSimplify: Boolean = false,
    ) {
        log(LogEntity(logLevel, logMessage, logLabelGroup, isSimplify))
    }

    //trace
    fun trace(
        message: String,
        logLabelGroup: LogLabelGroup = LogLabelGroup.trace0,
        isSimplify: Boolean = false,
    ) {
        log(LogLevel.trace, message, logLabelGroup, isSimplify)
    }

    fun String.toTrace(
        logLabelGroup: LogLabelGroup = LogLabelGroup.trace0,
        isSimplify: Boolean = false,
    ) {
        trace(this, logLabelGroup, isSimplify)
    }

    //debug
    fun debug(
        message: String,
        logLabelGroup: LogLabelGroup = LogLabelGroup.trace0,
        isSimplify: Boolean = false,
    ) {
        log(LogLevel.debug, message, logLabelGroup, isSimplify)
    }

    //warn
    fun warn(
        message: String,
        logLabelGroup: LogLabelGroup = LogLabelGroup.trace0,
        isSimplify: Boolean = false,
    ) {
        log(LogLevel.warn, message, logLabelGroup, isSimplify)
    }

    //error
    fun error(
        message: String,
        logLabelGroup: LogLabelGroup = LogLabelGroup.trace0,
        isSimplify: Boolean = false,
    ) {
        log(LogLevel.error, message, logLabelGroup, isSimplify)
    }

    //fatal
    fun fatal(
        message: String,
        logLabelGroup: LogLabelGroup = LogLabelGroup.trace0,
        isSimplify: Boolean = false,
    ) {
        log(LogLevel.fatal, message, logLabelGroup, isSimplify)
    }
}