package helper

import helper.base.DebugHelper.Debugger.Companion.getDebuggerByInt
import helper.base.DebugHelper.DebuggerList
import org.junit.Test

internal class DebugHelperTest {
    @Test
    fun t1() {
        val debuggerList = DebuggerList(
            arrayListOf(
                getDebuggerByInt(1),
                getDebuggerByInt(0),
            )
        )

        debuggerList.pln("d_a", arrayListOf(0, 1))
        debuggerList.pln("d_b", arrayListOf(1))
        debuggerList.pln("d_c", arrayListOf(0))
    }
}