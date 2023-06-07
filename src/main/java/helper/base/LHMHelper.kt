package helper.base

import helper.base.LHMHelper.LHMExpand.add
import java.lang.IllegalArgumentException

object LHMHelper {
    object LHMExpand {
        fun Array<out Pair<String, Double>>.toLinkedHashMap(): LinkedHashMap<String, Double> {
            return LinkedHashMap<String, Double>().also { lhm ->
                this.toList().map { (s, d): Pair<String, Double> ->
                    lhm[s] = d
                }
            }
        }

        fun <A1, A2> LinkedHashMap<A1, A2>.toStr(
            tabNum: Int = 0,
        ): String {
            val tabNum1 = tabNum + 1
            val tabNumStr = "\t".repeat(tabNum)
            val tabNumStr1 = "\t".repeat(tabNum1)
            val sb = StringBuilder()
            this.map { (a1, a2) ->
                sb.append("${tabNumStr}${a1}\n")
                sb.append("${tabNumStr1}${a2}\n")
            }
            return sb.toString()
        }

        // 确定了，不能重复add
        // 想修改的话，用原始的lhm[k]=v
        fun <A1, A2> LinkedHashMap<A1, A2>.add(
            a1: A1,
            a2: A2,
        ) {
            if (!this.containsKey(a1)) {
                this[a1] = a2
            }
        }
    }

    open class A3LHM<A1, A2, A3> : LinkedHashMap<A1, LinkedHashMap<A2, A3>>() {
        fun add(
            a1: A1,
            a2: A2,
            a3: A3,
        ) {
            if (!this.containsKey(a1)) {
                this[a1] = LinkedHashMap()
            }
            this[a1]!!.add(a2, a3)
        }

        open fun touch(
            init: (A1, A2, A3) -> Unit,
        ) {
            this.map { (a1, a1v) ->
                a1v.map { (a2, a3) ->
                    init(a1, a2, a3)
                }
            }
        }

        fun toStr(
            tabNum: Int = 0,
        ): String {
            val tabNum1 = tabNum + 1
            val tabNum2 = tabNum + 2
            val tabNumStr = "\t".repeat(tabNum)
            val tabNumStr1 = "\t".repeat(tabNum1)
            val tabNumStr2 = "\t".repeat(tabNum2)
            val sb = StringBuilder()
            this.touch { a1, a2, a3 ->
                sb.append("${tabNumStr}${a1}\n")
                sb.append("${tabNumStr1}${a2}\n")
                sb.append("${tabNumStr2}${a3}\n")
            }
            return sb.toString()
        }
    }

    open class A4LHM<A1, A2, A3, A4> : LinkedHashMap<A1, A3LHM<A2, A3, A4>>() {
        fun add(
            a1: A1,
            a2: A2,
            a3: A3,
            a4: A4,
        ) {
            if (!this.containsKey(a1)) {
                this[a1] = A3LHM()
            }
            this[a1]!!.add(a2, a3, a4)
        }

        open fun touch(
            init: (A1, A2, A3, A4) -> Unit,
        ) {
            this.map { (a1, a1v) ->
                a1v.touch { a2, a3, a4 ->
                    init(a1, a2, a3, a4)
                }
            }
        }

        fun toPrintln() {
            this.touch { a1, a2, a3, a4 ->
                println("${a1},${a2},${a3},${a4}")
            }
        }
    }

    open class A5LHM<A1, A2, A3, A4, A5> : LinkedHashMap<A1, A4LHM<A2, A3, A4, A5>>() {
        fun add(
            a1: A1,
            a2: A2,
            a3: A3,
            a4: A4,
            a5: A5,
        ) {
            if (!this.containsKey(a1)) {
                this[a1] = A4LHM()
            }
            this[a1]!!.add(a2, a3, a4, a5)
        }

        fun touch(
            init: (A1, A2, A3, A4, A5) -> Unit,
        ) {
            this.map { (a1, a1v) ->
                a1v.touch { a2, a3, a4, a5 ->
                    init(a1, a2, a3, a4, a5)
                }
            }
        }
    }
}