package helper.tuTest

import helper.tu.TuHelper

//并查集检测环路
object CycleTest {
    object T1 {
        @JvmStatic
        fun main(args: Array<String>) {
            val edges1 = arrayListOf(arrayListOf(0, 1), arrayListOf(1, 2), arrayListOf(2, 3), arrayListOf(3, 4))
            println(TuHelper.WuXiangHelper.hasCycle(edges1)) // false
            val edges2 =
                arrayListOf(arrayListOf(0, 1), arrayListOf(1, 2), arrayListOf(2, 3), arrayListOf(3, 4), arrayListOf(4, 0))
            println(TuHelper.WuXiangHelper.hasCycle(edges2)) // true
        }
    }
}