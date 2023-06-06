package helper.scxml.scxml1.tests1

import helper.scxml.scxml1.Scxml1Helper
import org.junit.Test

internal class OtherTest {
    @Test
    fun testParallelStateMachine() {
        class TestParallelStateMachine : Scxml1Helper.StateMachine(
            "scxml/tests1/test4_parallel.scxml",
        ) {
            fun p() {
                println("STATE: p")
            }

            fun S1() {
                println("STATE: S1")
            }

            fun S11() {
                println("STATE: S11")
            }

            fun S12() {
                println("STATE: S12")
            }

            fun S2() {
                println("STATE: S2")
            }

            fun S21() {
                println("STATE: S21")
            }

            fun S22() {
                println("STATE: S22")
            }

            fun S() {
                println("STATE: S")
            }

            fun s1() {
                println("STATE: s1")
            }

            fun s11() {
                println("STATE: s11")
            }

            fun s12() {
                println("STATE: s12")
            }

            fun s2() {
                println("STATE: s2")
            }
        }
        TestParallelStateMachine()
    }

    @Test
    fun testInitialStateMachine() {
        class TestInitialStateMachine : Scxml1Helper.StateMachine(
            "scxml/tests1/test5_test_initial.scxml",
        ) {
            fun new_node28() {
                println("STATE: new_node28")
            }

            fun new_node30() {
                println("STATE: new_node30")
            }

            fun n1() {
                println("STATE: n1")
            }

            fun n2() {
                println("STATE: n2")
            }
        }
        TestInitialStateMachine()
    }
}