package helper.scxml.scxml2.t2_traffic.env

import java.io.File

class RLTransition(
    var rlState: RLState = RLState(),
    var event: String = "",
    var rlResult: RLResult = RLResult(),
) {
    var reward: Int
        get() {
            return rlResult.reward
        }
        set(value) {
            rlResult.reward = value
        }

    var nextRLState: RLState
        get() {
            return rlResult.nextRLState
        }
        set(value) {
            rlResult.nextRLState = value
        }


    var done: Boolean
        get() {
            return rlResult.done
        }
        set(value) {
            rlResult.done = value
        }

    override fun toString(): String {
        return "(${rlState},${event},${rlResult})"
    }

    fun writeToFile(file: File) {
        file.appendText("${this}\n")
    }

    fun readyForNext() {
        this.rlState = this.nextRLState.clone()
        this.event = ""
    }
}