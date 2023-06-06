package helper.scxml.scxml2.t7_cycle

import helper.DebugHelper.getDebuggerList
import org.junit.Test

internal class Test {
    @Test
    fun t1t1() {
        val env = EnvHelper.getEnvObj1()
        val rs = env.toStr()
        println(rs)
    }

    @Test
    fun t1t2() {
        val debuggerList = getDebuggerList(
            0,
            1,
        )
        val env = EnvHelper.getEnvObj1()
        repeat(10) {
            debuggerList.pln("${"-".repeat(10)}repeat${it}", arrayListOf(0, 1))
            env.reset()
            env.taskRun(debuggerList)
        }
    }
}