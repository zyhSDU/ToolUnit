package helper.scxml.scxml1.tests1

import helper.scxml.scxml1.Scxml1Helper
import org.apache.commons.scxml.model.State
import org.apache.commons.scxml.model.Transition
import org.junit.Test

internal class AtmTest {
    @Test
    fun testAtm() {
        class AtmStateMachine : Scxml1Helper.StateMachine(
            "scxml/tests1/test2_atm.scxml",
        ) {
            fun getCurrentState(): State {
                //getEngine是父类AbstractStateMachine自带的方法
                return engine.currentStatus.states.iterator().next() as State
            }

            //stateId就是状态名
            fun getCurrentStateId(): String? {
                return getCurrentState().id
            }

            fun getEventNames(): String {
                val sb = StringBuilder()
                this.engine.stateMachine.children.values.filterNotNull().map { state ->
                    state as State
                    state.transitionsList.filterNotNull().map { transition ->
                        transition as Transition
                        sb.append(transition.event)
                        sb.append(",")
                    }
                }
                return sb.substring(0, sb.length - 2)
            }

            fun idle() {
                println("STATE: idle")
            }

            fun loading() {
                println("STATE: loading")
            }

            fun inService() {
                println("STATE: inService")
            }

            fun outOfService() {
                println("STATE: outOfService")
            }

            fun disconnected() {
                println("STATE: disconnected")
            }
        }

        val stateMachine = AtmStateMachine()
        stateMachine.printCurrentStatusInfo()

        fun fireEventFun(string: String) {
            println(string)
            stateMachine.fireEvent(string)
            stateMachine.printCurrentStatusInfo()
        }
        arrayOf(
            "atm.connected",
            "atm.loadSuccess",
            "atm.shutdown",
        ).map {
            fireEventFun(it)
        }
    }
}