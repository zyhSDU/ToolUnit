package helper.scxml.scxml1.tests1

import helper.scxml.scxml1.Scxml1Helper
import org.junit.Test

internal class HelloTest {
    @Test
    fun testHelloStateMachine() {
        class HelloStateMachine : Scxml1Helper.StateMachine(
            "scxml/tests1/test3_hello.scxml",
        ) {
            fun hello() {
                println("STATE: hello")
            }

            fun end() {
                println("STATE: end")
            }
        }

        val sm = HelloStateMachine()
        sm.engine.stateMachine.initial.run {
            println(this)
        }
    }
}