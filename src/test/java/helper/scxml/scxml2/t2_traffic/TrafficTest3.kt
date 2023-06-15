package helper.scxml.scxml2.t2_traffic

import helper.base.DebugHelper.DebuggerList
import helper.base.DebugHelper.DebuggerList.Companion.getDebuggerList
import helper.base.FileHelper.toFile
import helper.base.ProgressBarHelper
import helper.base.RegexHelper.match
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.t2_traffic.fun_strategy.FunStrategyHelper.StrIntStrIntLHM
import helper.scxml.scxml2.t2_traffic.fun_strategy.FunStrategyHelper.taskRun
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.StateDataRenEventSelector.Expand.toLearnedRenEventSelector
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.dEnvStateConstraintLHM
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.rEnvStateConstraintLHM
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.stateRenEventSelector1
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.stateRenEventSelector2
import java.io.File

internal class TrafficTest3 {
    object Test {
        fun getMaxIntInDir(
            file: File,
            prefix: String,
            suffix: String,
        ): Int? {
            assert(file.exists() && file.isDirectory)
            return file.listFiles().mapNotNull {
                it.name.match(Regex("${prefix}(\\d+)\\.${suffix}"))?.let {
                    it.groupValues[1].toInt()
                }
            }.maxOrNull()
        }

        fun trainModel(
            modelDirString: String? = null,
            stateEnvConstraintLHM: LinkedHashMap<String, ClockConstraint> = rEnvStateConstraintLHM,
            IRenEventSelector: IRenEventSelector = stateRenEventSelector1,
            repeatNum: Int = 10000,
            debuggerList: DebuggerList = getDebuggerList(0),
            ifBar: Boolean = true,
        ) {
            val modelTrainDirString = "${modelDirString}/train"
            var a4LHM: StrIntStrIntLHM? = null
            var newFile: File? = null
            if (modelDirString != null) {
                val file = File(modelTrainDirString)
                file.mkdirs()
                getMaxIntInDir(
                    file,
                    "train",
                    "txt",
                )?.let {
                    "${modelTrainDirString}/train${it}.txt".let {
                        a4LHM = StrIntStrIntLHM.getLHMFromFile(it)
                    }
                    "${modelTrainDirString}/train${it + repeatNum}.txt".let {
                        newFile = File(it)
                    }
                }
            }
            if (a4LHM == null) {
                a4LHM = StrIntStrIntLHM.getInitialLHM()
                "${modelTrainDirString}/train${repeatNum}.txt".let {
                    newFile = File(it)
                }
            }

            repeat(repeatNum) {
                taskRun(
                    outA4LHM = a4LHM!!,
                    envStateConstraintLHM = stateEnvConstraintLHM,
                    envEventLHM = StrategyTripleHelper.envEventLHM1,
                    getIRenEventSelectorFun = { IRenEventSelector },
                    debuggerList = debuggerList,
                )
                if (ifBar) {
                    ProgressBarHelper.printProgressBar(it, repeatNum)
                }
            }

            a4LHM!!.writeToFile(newFile!!)
        }

        fun testModel(
            modelDirString: String? = null,
            index: Int? = null,
            stateEnvConstraintLHM: LinkedHashMap<String, ClockConstraint> = rEnvStateConstraintLHM,
            repeatNum: Int = 1,
            debuggerList: DebuggerList = getDebuggerList(0),
            ifBar: Boolean = true,
        ) {
            //index为null，以冒号后面内容为取值内容
            val modelTrainDirString = "${modelDirString}/train"
            val modelTestDirString = "${modelDirString}/test"
            val modelTrainIndex = index ?: getMaxIntInDir(
                File(modelTrainDirString),
                "train",
                "txt",
            )
            val modelTrainString = "${modelTrainDirString}/train${modelTrainIndex}.txt"
            val a4LHM = StrIntStrIntLHM.getLHMFromFile(modelTrainString)

            val modelTestModelDirString = "${modelTestDirString}/train${modelTrainIndex}"
            val modelTestModelDir = modelTestModelDirString.toFile()
            modelTestModelDir.mkdirs()

            val maxTestIndex = getMaxIntInDir(
                modelTestModelDir,
                "test",
                "txt",
            ) ?: 0
            var a4LHM2: StrIntStrIntLHM? = null
            if (maxTestIndex != 0) {
                a4LHM2 = StrIntStrIntLHM.getLHMFromFile(
                    "${modelTestModelDirString}/test${maxTestIndex}.txt"
                )
            }
            if (a4LHM2 == null) {
                a4LHM2 = StrIntStrIntLHM.getInitialLHM()
            }

            val newFileIndex = maxTestIndex + repeatNum
            val newFile = File("${modelTestModelDir}/test${newFileIndex}.txt")

            repeat(repeatNum) {
                taskRun(
                    outA4LHM = a4LHM2,
                    envStateConstraintLHM = stateEnvConstraintLHM,
                    envEventLHM = StrategyTripleHelper.envEventLHM1,
                    getIRenEventSelectorFun = { scxmlTuple ->
                        a4LHM.toLearnedRenEventSelector(scxmlTuple)
                    },
                    debuggerList = debuggerList,
                )
                if (ifBar) {
                    ProgressBarHelper.printProgressBar(it, repeatNum)
                }
            }

            a4LHM2.touch { a1, a2, a3, a4 ->
                newFile.appendText("${a1},${a2},${a3},${a4}\n")
            }
        }

        fun model1(
            stateEnvConstraintLHM: LinkedHashMap<String, ClockConstraint> = rEnvStateConstraintLHM,
            debuggerList: DebuggerList = getDebuggerList(0),
        ) {
            val modelDirString = "out/log/model/traffic/model1"
            trainModel(
                modelDirString = modelDirString,
                stateEnvConstraintLHM = stateEnvConstraintLHM,
                IRenEventSelector = stateRenEventSelector1,
                repeatNum = 100000,
                debuggerList = debuggerList,
                ifBar = true,
            )
            testModel(
                modelDirString = modelDirString,
                index = null,
                stateEnvConstraintLHM = stateEnvConstraintLHM,
                repeatNum = 1000,
                debuggerList = debuggerList,
                ifBar = false,
            )
        }

        fun model2(
            stateEnvConstraintLHM: LinkedHashMap<String, ClockConstraint> = rEnvStateConstraintLHM,
            debuggerList: DebuggerList = getDebuggerList(0),
        ) {
            val modelDirString = "out/log/model/traffic/model2"
            trainModel(
                modelDirString = modelDirString,
                stateEnvConstraintLHM = stateEnvConstraintLHM,
                IRenEventSelector = stateRenEventSelector2,
                repeatNum = 10,
                debuggerList = debuggerList,
            )
            testModel(
                modelDirString = modelDirString,
                index = null,
                stateEnvConstraintLHM = stateEnvConstraintLHM,
                repeatNum = 1000,
                debuggerList = debuggerList,
            )
        }

        fun model3(
            stateEnvConstraintLHM: LinkedHashMap<String, ClockConstraint> = dEnvStateConstraintLHM,
            debuggerList: DebuggerList = getDebuggerList(0),
            ifBar: Boolean = true,
        ) {
            val modelDirString = "out/log/model/traffic/model3"
            trainModel(
                modelDirString = modelDirString,
                stateEnvConstraintLHM = stateEnvConstraintLHM,
                IRenEventSelector = stateRenEventSelector1,
                repeatNum = 10000,
                debuggerList = debuggerList,
                ifBar = ifBar,
            )
            testModel(
                modelDirString = modelDirString,
                index = null,
                stateEnvConstraintLHM = stateEnvConstraintLHM,
                repeatNum = 1000,
                debuggerList = debuggerList,
                ifBar = ifBar,
            )
        }

        @JvmStatic
        fun main(args: Array<String>) {
            model3()
        }
    }
}
