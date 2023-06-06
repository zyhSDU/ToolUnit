package helper.scxml.scxml1.tests4

import helper.scxml.Scxml1Helper

internal class OtherTest {
    class SwitchMachine : Scxml1Helper.StateMachine(
        "scxml2/t_factory/switch.scxml"
    ) {
        fun off() {
            println("state:off")
        }

        fun on() {
            println("state:on")
        }
    }

    object Tests {
        @JvmStatic
        fun main(args: Array<String>) {
            val switchMachine = SwitchMachine()
            switchMachine.testRun()
        }
    }
}
