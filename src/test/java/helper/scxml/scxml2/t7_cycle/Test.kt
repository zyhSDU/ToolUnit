package helper.scxml.scxml2.t7_cycle

import helper.DebugHelper.Debugger
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
        val env = EnvHelper.getEnvObj1()
        env.taskRun(
            debuggerList = getDebuggerList(
                Debugger(1),
                Debugger(0),
            ),
        )
        //taskRun只到时钟89？
    }
}