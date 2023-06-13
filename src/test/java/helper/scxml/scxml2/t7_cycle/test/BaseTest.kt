package helper.scxml.scxml2.t7_cycle.test

import helper.base.DebugHelper.getDebuggerList
import helper.scxml.scxml2.EnvHelper.RunResult
import helper.scxml.scxml2.t2_traffic.Res
import helper.scxml.scxml2.t7_cycle.EnvObjHelper
import org.junit.Test

internal class BaseTest {
    @Test
    fun t1t1() {
        val env = EnvObjHelper.getEnvObj1()
        val rs = env.toStr()
        println(rs)
    }

    @Test
    fun t1t2t1() {
        val debuggerList = getDebuggerList(
            0,
            1,
        )
        val env = EnvObjHelper.getEnvObj1()
        repeat(100) {
            debuggerList.pln(
                "${"-".repeat(10)}repeat${it}",
                arrayListOf(
                    0,
                    1,
                )
            )
            env.reset()
            val runResult = RunResult()
            env.taskRun2(
                runResult = runResult,
                debuggerList = debuggerList,
            )
            println(runResult)
        }
    }

    @Test
    fun t1t2t2() {
        val debuggerList = getDebuggerList(
            0,
            0,
        )
        val rrs = ArrayList<RunResult>()
        val env = EnvObjHelper.getEnvObj1()
        repeat(100) {
            env.reset()
            env.taskRun(
                debuggerList = debuggerList
            ).let {
                rrs.add(it)
            }
        }
        val sorted = rrs.sortedBy {
            it.endData[Res.globalTimeId]!!.toInt()
        }
        sorted.map {
            println(it)
        }
    }

    @Test
    fun t2t1() {
        val debuggerList = getDebuggerList(
            0,
            0,
        )
        val rrs = ArrayList<RunResult>()
        val env = EnvObjHelper.getEnvObj2()
        repeat(100000) {
            env.reset()
            env.taskRun(
                debuggerList = debuggerList,
            ).let {
                rrs.add(it)
            }
        }
        rrs.map {
            it.endData[Res.globalTimeId]!!.toInt()
        }.average().let {
            println(it)
        }
        //145
    }

    @Test
    fun t3t1() {
        val debuggerList = getDebuggerList(
            0,
            0,
        )
        val rrs = ArrayList<RunResult>()
        val env = EnvObjHelper.getEnvObj3()
        repeat(1000000) {
            println(it)
            env.reset()
            env.taskRun(
                debuggerList = debuggerList,
            ).let {
                rrs.add(it)
            }
        }
        rrs.map {
            it.endData["c"]!!.toInt()
        }.average().let {
            println(it)
        }
        //理论204
        //实际203.223718
    }
}