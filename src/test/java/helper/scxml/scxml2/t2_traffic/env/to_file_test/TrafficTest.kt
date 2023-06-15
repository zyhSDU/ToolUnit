package helper.scxml.scxml2.t2_traffic.env.to_file_test

import helper.base.DebugHelper.DebuggerList
import helper.base.DebugHelper.DebuggerList.Companion.getDebuggerList
import helper.base.FileHelper
import helper.base.PrintHelper.StringTo.toPrintln
import helper.scxml.scxml2.t2_traffic.env.EnvHelper.Env
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.envEventLHM1
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.rEnvStateConstraintLHM
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.stateRenEventSelector1
import java.io.File
import kotlin.math.pow

object TrafficTest {
    private fun repeatTaskRun(
        getEnvFun: () -> Env,
        repeatTaskDirString: String,
        debuggerList: DebuggerList,
    ) {
        val trafficEnv = getEnvFun()

        val ifCleanFile = true
        val ifFileAppendTextTaskRunString = false

        File(repeatTaskDirString).mkdirs()
        val outTransitionFile = File("${repeatTaskDirString}\\outTransition.txt")
        val countStateFile = File("${repeatTaskDirString}\\countState.txt")
        val countStateSet = HashSet<String>()

        if (ifCleanFile) {
            outTransitionFile.writeText("")
            countStateFile.writeText("")
        }

        debuggerList.startPln("repeat")
        val repeatTimes = 10000
        repeat(repeatTimes) {
            val taskRunString = "taskRun${it}"
            debuggerList.startPln(taskRunString)
            if (ifFileAppendTextTaskRunString) {
                outTransitionFile.appendText("\t${taskRunString}\n")
            }

            trafficEnv.taskRun(
                outTransitionFile = outTransitionFile,
                countStateSet = countStateSet,
                debuggerList = debuggerList,
            )

            debuggerList.endPln()
        }
        debuggerList.endPln()
        countStateSet.map {
            countStateFile.appendText("${it}\n")
        }
    }

    object Test1 {
        private fun getTrafficEnv() = Env(
            envStateConstraintLHM = rEnvStateConstraintLHM,
            envEventLHM = envEventLHM1,
            getIRenEventSelectorFun = {
                stateRenEventSelector1
            }
        )

        @JvmStatic
        fun main(args: Array<String>) {
            repeat(10000) {
                val newMaxIndexDirFileString: String = FileHelper.getNewMaxIndexDirFileString(
                    "D:/aTrafficTask/kt_out",
                    "task",
                )
                newMaxIndexDirFileString.toPrintln()
                repeatTaskRun(
                    getEnvFun = Test1::getTrafficEnv,
                    repeatTaskDirString = newMaxIndexDirFileString,
                    debuggerList = getDebuggerList(0),
                )
            }
        }
    }

    object T2 {
        val kt_out2_dir_string = "D:/aTrafficTask/kt_out_2"

        object Test2T1 {
            private fun getTrafficEnv() = Env(
                envStateConstraintLHM = rEnvStateConstraintLHM,
                envEventLHM = StrategyTripleHelper.getBaseEnvEventLHM().also {
                    it["Car"] = linkedMapOf(
                        "car_easy" to 1.0,
                        "car_heavy" to 1.0,
                    )
                    it["Train"] = linkedMapOf(
                        "train_go" to 1.0,
                        "train_wait" to 1.0
                    )
                },
                getIRenEventSelectorFun = {
                    stateRenEventSelector1
                }
            )

            @JvmStatic
            fun main(args: Array<String>) {
                repeat(10000) {
                    val newMaxIndexDirFileString = FileHelper.getNewMaxIndexDirFileString(
                        dirString = kt_out2_dir_string,
                        fileString = "task",
                    )
                    newMaxIndexDirFileString.toPrintln()
                    repeatTaskRun(
                        getEnvFun = Test2T1::getTrafficEnv,
                        repeatTaskDirString = newMaxIndexDirFileString,
                        debuggerList = getDebuggerList(0),
                    )
                }
            }
        }

        object Test2T2 {
            @JvmStatic
            fun main(args: Array<String>) {
                val ts = LinkedHashMap<String, Int>()
                var countT = 0
                var file_index = 0
                while (true) {
                    val file_str = "$kt_out2_dir_string/task${file_index}/outTransition.txt"
                    val file = File(file_str)
                    if (!file.exists()) break
                    file.useLines { lines ->
                        lines.forEach {
                            if (!ts.containsKey(it)) {
                                ts[it] = 1
                                println("${ts.size},${countT}")
                            } else {
                                ts[it] = ts[it]!! + 1
                            }
                            countT += 1
                        }
                    }
                    file_index += 1
                }

                val file = File("$kt_out2_dir_string/ts.txt")
                file.writeText("")
                ts.map { (k, v) ->
                    file.appendText("${k}\n")
                }

                val file2 = File("$kt_out2_dir_string/ts2.txt")
                file2.writeText("")
                ts.map { (k, v) ->
                    file2.appendText("${k},${v}\n")
                }
            }
        }
    }


    private fun getTrafficEnvBias(
        train_wait_bias: Double
    ) = Env(
        envStateConstraintLHM = rEnvStateConstraintLHM,
        envEventLHM = StrategyTripleHelper.getBaseEnvEventLHM().also {
            it["Car"] = linkedMapOf(
                "car_easy" to 1.0,
                "car_heavy" to 1.0,
            )
            it["Train"] = linkedMapOf(
                "train_go" to 1.0,
                "train_wait" to train_wait_bias
            )
        },
        getIRenEventSelectorFun = {
            stateRenEventSelector1
        }
    )

    object Test3 {
        val out_dir_string = "D:/aTrafficTask/kt_out_3"

        object Test3T1 {
            private fun getTrafficEnv() = getTrafficEnvBias(4.0)

            @JvmStatic
            fun main(args: Array<String>) {
                repeat(10000) {
                    val newMaxIndexDirFileString = FileHelper.getNewMaxIndexDirFileString(
                        dirString = out_dir_string,
                        fileString = "task",
                    )
                    newMaxIndexDirFileString.toPrintln()
                    repeatTaskRun(
                        getEnvFun = Test3T1::getTrafficEnv,
                        repeatTaskDirString = newMaxIndexDirFileString,
                        debuggerList = getDebuggerList(0),
                    )
                }
            }
        }

        object Test3T2 {
            @JvmStatic
            fun main(args: Array<String>) {
                val out_dir_string = out_dir_string
                val ts = LinkedHashMap<String, Int>()
                var countT = 0
                var file_index = 0
                while (true) {
                    val file_str = "${out_dir_string}/task${file_index}/outTransition.txt"
                    val file = File(file_str)
                    if (!file.exists()) break
                    file.useLines { lines ->
                        lines.forEach {
                            if (!ts.containsKey(it)) {
                                ts[it] = 1
                                println("${ts.size},${countT}")
                            } else {
                                ts[it] = ts[it]!! + 1
                            }
                            countT += 1
                        }
                    }
                    file_index += 1
                }

                val file = File("${out_dir_string}/ts.txt")
                file.writeText("")
                ts.map { (k, v) ->
                    file.appendText("${k}\n")
                }

                val file2 = File("${out_dir_string}/ts2.txt")
                file2.writeText("")
                ts.map { (k, v) ->
                    file2.appendText("${k},${v}\n")
                }
            }
        }
    }

    object Test4 {
        val out_dir_string = "D:/aTrafficTask/kt_out_4"

        object T1 {
            private fun getTrafficEnv() = getTrafficEnvBias(16.0)

            @JvmStatic
            fun main(args: Array<String>) {
                repeat(10000) {
                    val newMaxIndexDirFileString = FileHelper.getNewMaxIndexDirFileString(
                        dirString = out_dir_string,
                        fileString = "task",
                    )
                    newMaxIndexDirFileString.toPrintln()
                    repeatTaskRun(
                        getEnvFun = T1::getTrafficEnv,
                        repeatTaskDirString = newMaxIndexDirFileString,
                        debuggerList = getDebuggerList(0),
                    )
                }
            }
        }

        object T2 {
            @JvmStatic
            fun main(args: Array<String>) {
                val out_dir_string = out_dir_string
                val ts = LinkedHashMap<String, Int>()
                var countT = 0
                var file_index = 0
                while (true) {
                    if (file_index >= 1650) break
                    val file_str = "${out_dir_string}/task${file_index}/outTransition.txt"
                    val file = File(file_str)
                    if (!file.exists()) break
                    file.useLines { lines ->
                        lines.forEach {
                            if (!ts.containsKey(it)) {
                                ts[it] = 1
                                println("${ts.size},${countT}")
                            } else {
                                ts[it] = ts[it]!! + 1
                            }
                            countT += 1
                        }
                    }
                    file_index += 1
                }
                val file = File("${out_dir_string}/ts.txt")
                val file2 = File("${out_dir_string}/ts2.txt")
                file.writeText("")
                file2.writeText("")
                ts.map { (k, v) ->
                    file.appendText("${k}\n")
                    file2.appendText("${k},${v}\n")
                }
            }
        }
    }

    object CalcuteTest {
        @JvmStatic
        fun main(args: Array<String>) {
            val maxTry = 25
            (1..32).map { bias ->
                val v0 = 1.0 / bias
                val v25 = ((bias - 1.0) / bias).pow(maxTry.toDouble())
                println("${bias},${v0},${v25}")
            }
        }
    }
}