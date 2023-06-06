package helper.scxml.scxml1.tests2

import helper.scxml.Scxml1Helper
import org.junit.Test

internal class OtherTest {
    @Test
    fun testScxmlTutorialTest1() {
        class T01 : Scxml1Helper.StateMachine(
            "scxml/tests2/t01_hello.scxml",
        )
        T01()
    }

    @Test
    fun testScxmlTutorialTest2() {
        class T02 : Scxml1Helper.StateMachine(
            "scxml/tests2/t02.scxml",
        ) {
            fun testRun2() {
                val stateMachine = this
                Thread {
                    var ifStart = false
                    while (true) {
                        Thread.sleep(4000)
                        if (ifStart) {
                            ifStart = false
                            stateMachine.fireEvent("Shutdown.1")
                            stateMachine.fireEvent("Shutdown.2")
                        } else {
                            ifStart = true
                            stateMachine.fireEvent("Start.1")
                            stateMachine.fireEvent("Start.2")
                        }
                    }
                }.start()
                stateMachine.testRun()
            }
        }
        T02()
    }

    @Test
    fun testScxmlTutorialTest5() {
        class T05 : Scxml1Helper.StateMachine(
            "scxml/tests2/t05_timer.scxml",
        )
        T05()
    }
}