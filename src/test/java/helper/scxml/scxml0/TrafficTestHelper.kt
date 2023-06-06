package helper.scxml.scxml0

import helper.base.LHMHelper
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.Scxml2Helper
import helper.scxml.strategy.ScxmlOneStrategyHelper.SNode

object TrafficTestHelper {
    fun getSCXMLTuple(): SCXMLTuple {
        return Scxml2Helper.getSCXMLTuple("scxml2/traffic.scxml")
    }

    fun getSNode1(): SNode {
        return SNode.getRootNode().also {
            it.addMiddleNode1(
                "Aalborg", "0 <= T <= 2",
                "bike" to 1.0
            )
            it.addMiddleNode1(
                "Bike", "42 <= T <= 45",
                "bike_end" to 1.0
            )
        }
    }

    fun getSNode2(): SNode {
        return SNode.getRootNode().also {
            it.addMiddleNode1(
                "Aalborg", "0 <= T <= 2",
                "car" to 1.0,
            )
            it.addMiddleNode1(
                "Car", "0 <= T <= 0",
                "car_easy" to 10.0,
                "car_heavy" to 1.0,
            )
            it.addMiddleNode1(
                "Easy", "20 <= T <= 20",
                "car_easy_end" to 1.0
            )
            it.addMiddleNode1(
                "Heavy", "140 <= T <= 140",
                "car_heavy_end" to 1.0
            )
        }
    }

    fun getSNode3(): SNode {
        return SNode.getRootNode().also {
            it.addMiddleNode1(
                "Aalborg", "3 <= globalTime <= 60",
                "bike" to 1.0,
            )
            it.addMiddleNode1(
                "Bike", "42 <= T <= 45",
                "bike_end" to 1.0
            )
            it.addMiddleNode1(
                "Aalborg", "0 <= globalTime <= 2",
                "train" to 1.0,
            )
            it.addMiddleNode1(
                "Train", "4 <= T <= 6",
                "train_go" to 10.0,
                "train_wait" to 1.0
            )
            it.addMiddleNode1(
                "Go", "35 <= T <= 35",
                "train_go_end" to 1.0
            )
            it.addMiddleNode1(
                "Wait", "0 <= globalTime <= 2",
                "train_wait_train" to 1.0
            )
            it.addMiddleNode1(
                "Wait", "3 <= globalTime <= 60",
                "train_wait_back" to 1.0
            )
            it.addMiddleNode1(
                "GoBack", "0 <= T <= 0",
                "back_back" to 1.0
            )
        }
    }

    fun getSNode4_randomSNode(): SNode {
        return SNode.getRootNode().also {
            it.addMiddleNode1(
                "Aalborg", "0 <= T <= 2",
                "bike" to 1.0,
                "car" to 1.0,
                "train" to 1.0,
            )
            it.addMiddleNode1(
                "Bike", "42 <= T <= 45",
                "bike_end" to 1.0
            )
            it.addMiddleNode1(
                "Car", "0 <= T <= 0",
                "car_easy" to 10.0,
                "car_heavy" to 1.0,
            )
            it.addMiddleNode1(
                "Easy", "20 <= T <= 20",
                "car_easy_end" to 1.0
            )
            it.addMiddleNode1(
                "Heavy", "140 <= T <= 140",
                "car_heavy_end" to 1.0
            )
            it.addMiddleNode1(
                "Train", "4 <= T <= 6",
                "train_go" to 10.0,
                "train_wait" to 1.0
            )
            it.addMiddleNode1(
                "Go", "35 <= T <= 35",
                "train_go_end" to 1.0
            )
            it.addMiddleNode1(
                "Wait", "0 <= T <= 2",
                "train_wait_train" to 1.0,
                "train_wait_back" to 1.0,
            )
            it.addMiddleNode1(
                "GoBack", "0 <= T <= 0",
                "back_back" to 1.0
            )
        }
    }

    data class SIST(
        val stateId: String,
        val time: Int,
        val chosenEvent: String,
        var unSafeCount: Int = 0,
        val safeResults: ArrayList<Int> = ArrayList(),
    ) {
        fun toTextString(): String {
            val sb = StringBuilder()
            sb.append("${stateId},\t")
            sb.append("${time},\t")
            sb.append("${chosenEvent},\t")
            sb.append("${unSafeCount},\t")
            sb.append("${safeResults.size},\t")
            if (safeResults.size == 0) {
                sb.append("-1")
            } else {
                sb.append("${safeResults.sum() / safeResults.size}")
            }
            return sb.toString()
        }

        fun ifEmpty() = unSafeCount == 0 && safeResults.size == 0
    }

    fun LHMHelper.A4LHM<String, Int, String, SIST>.add(
        stateId: String,
        time: Int,
        chosenEvent: String,
    ) {
        this.add(stateId, time, chosenEvent, SIST(stateId, time, chosenEvent))
    }
}