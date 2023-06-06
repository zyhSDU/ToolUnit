package helper

import helper.base.BaseTypeHelper.ListExpand.toArrayList
import helper.base.BaseTypeHelper.toBoolean
import helper.base.BaseTypeHelper.toInt
import helper.base.MathHelper.modifiedSubtract
import helper.base.PrintHelper.StringTo.toPrintln

object DebugHelper {
    class DebuggerPlnType(
        val mainPrefix: String,
        val subPrefix: String,
    )

    val dPlnType1 = DebuggerPlnType("| ", "|-")
    val dPlnType2 = DebuggerPlnType("\t", "\t")

    class Debugger(
        val ifDebug: Boolean,
        var tabNum: Int = 0,
        val tabNumMax: Int = Int.MAX_VALUE,
        val debuggerPlnType: DebuggerPlnType = dPlnType1
    ) {
        constructor(
            ifDebug: Int,
            tabNum: Int = 0,
            tabNumMax: Int = Int.MAX_VALUE,
        ) : this(
            ifDebug.toBoolean(),
            tabNum,
            tabNumMax,
        )

        fun pln(
            string: String,
        ) {
            if (ifDebug && tabNum <= tabNumMax) {
                "${
                    debuggerPlnType.mainPrefix.repeat(
                        tabNum.modifiedSubtract(1)
                    )
                }${
                    debuggerPlnType.subPrefix.repeat(
                        tabNum.toBoolean().toInt()
                    )
                }${
                    string
                }".toPrintln()
            }
        }

        fun startPln(
            string: String,
        ) {
            this.pln(string)
            tabNum += 1
        }

        fun endPln() {
            tabNum -= 1
        }

        fun debug(
            init: () -> Unit,
        ) {
            if (ifDebug) {
                init()
            }
        }
    }

    class DebuggerList(
        val arr: ArrayList<Debugger> = ArrayList(),
    ) {
        fun pln(
            string: String,
            indexList: ArrayList<Int> = arrayListOf(0),
        ) {
            indexList.map {
                arr[it].pln(string)
            }
        }

        fun startPln(
            string: String,
            indexList: ArrayList<Int> = arrayListOf(0),
        ) {
            indexList.map {
                arr[it].startPln(string)
            }
        }

        fun endPln(
            indexList: ArrayList<Int> = arrayListOf(0),
        ) {
            indexList.map {
                arr[it].endPln()
            }
        }

        fun debug(
            init: () -> Unit,
            indexList: ArrayList<Int> = arrayListOf(0),
        ) {
            indexList.map {
                arr[it].debug(init)
            }
        }
    }

    fun getDebuggerList(
        vararg debugger: Debugger,
    ): DebuggerList {
        return DebuggerList(
            debugger.toArrayList()
        )
    }

    fun getDebuggerList(
        vararg ifDebug: Int,
    ): DebuggerList {
        return DebuggerList(
            ifDebug.map {
                Debugger(it)
            }.toArrayList()
        )
    }
}