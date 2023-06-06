package helper.scxml.scxml2.t2_traffic

import helper.base.LHMHelper
import helper.base.MathHelper
import helper.base.PrintHelper.StringTo.toPrintln
import helper.scxml.scxml2.Expand.SCXMLExecutorExpand.isInState
import helper.scxml.scxml2.Res
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml0.TrafficTestHelper
import org.apache.commons.scxml2.model.TransitionTarget
import org.junit.Test

internal class TrafficTest2 {
    class SUnit(
        val stateId: String,
        val dataExpr: Int,
        val intRange: IntRange,
        val stringDoubleLHM: LinkedHashMap<String, Double>,
    ) {
        constructor(
            stateId: String,
            dataExpr: Int,
            intRange: Int,
            stringDoubleLHM: LinkedHashMap<String, Double>,
        ) : this(
            stateId,
            dataExpr,
            intRange..intRange,
            stringDoubleLHM,
        )

        fun tryFire(
            scxmlTuple: SCXMLTuple,
            doOnEntry: (TransitionTarget) -> Unit = {},
            fireNext: () -> Unit,
        ): String? {
            var event: String? = null
            val executor = scxmlTuple.executor
            if (executor.isInState(stateId)) {
                if (dataExpr in intRange) {
                    MathHelper.getRandomStringWithLeftTime(
                        stringDoubleLHM,
                        intRange.last - dataExpr + 1,
                    )?.let {
                        scxmlTuple.fireEvent(it, doOnEntry)
                        event = it
                        fireNext()
                    }
                }
            }
            return event
        }
    }

    @Test
    fun testRunSNode1() {
        "-".repeat(80).toPrintln()
        val globalTimeId = Res.globalTimeId
        val scxmlTuple = TrafficTestHelper.getSCXMLTuple()
        val executor = scxmlTuple.executor
        val mySCXML = scxmlTuple.dataSCXML

        fun strategyFireEvent() {
            val dataT: Int = mySCXML.getDataInt("T")!!
            var ifFired = false
            arrayListOf(
                SUnit("Aalborg", dataT, 0..2, linkedMapOf("bike" to 1.0)),
                SUnit("Bike", dataT, 42..45, linkedMapOf("bike_end" to 1.0)),
            ).map {
                if (!ifFired) {
                    ifFired = it.tryFire(scxmlTuple, {}, ::strategyFireEvent) != null
                }
            }
        }

        executor.go()

        scxmlTuple.statusPrintln()
        while (true) {
            strategyFireEvent()
            if (mySCXML.ifDataExprEqualInt(globalTimeId, 60)) break
            if (executor.isInState("Sydney")) {
                break
            }
            mySCXML.setDataExprAddOne("T")
            mySCXML.setDataExprAddOne(globalTimeId)
        }
        scxmlTuple.statusPrintln()
    }

    @Test
    fun repeatTestRunSNode1() {
        repeat(12) {
            testRunSNode1()
        }
    }

    @Test
    fun testRunSNode2() {
        "-".repeat(80).toPrintln()
        val globalTimeId = Res.globalTimeId
        val scxmlTuple = TrafficTestHelper.getSCXMLTuple()
        val executor = scxmlTuple.executor
        val mySCXML = scxmlTuple.dataSCXML

        fun strategyFireEvent() {
            val dataT: Int = mySCXML.getDataInt("T")!!
            var ifFired = false
            arrayListOf(
                SUnit("Aalborg", dataT, 0..2, linkedMapOf("car" to 1.0)),
                SUnit("Car", dataT, 0..0, linkedMapOf("car_easy" to 1.0, "car_heavy" to 1.0)),
                SUnit("Easy", dataT, 20..20, linkedMapOf("car_easy_end" to 1.0)),
                SUnit("Heavy", dataT, 140..140, linkedMapOf("car_heavy_end" to 1.0)),
            ).map {
                if (!ifFired) {
                    ifFired = it.tryFire(scxmlTuple, {}, ::strategyFireEvent) != null
                }
            }
        }

        executor.go()

        scxmlTuple.statusPrintln()
        while (true) {
            strategyFireEvent()
            //如果激活状态改变，则需要重新查看是否有策略可以发动
            if (mySCXML.ifDataExprEqualInt(globalTimeId, 60)) break
            if (executor.isInState("Sydney")) {
                break
            }
            mySCXML.setDataExprAddOne("T")
            mySCXML.setDataExprAddOne(globalTimeId)
        }
        scxmlTuple.statusPrintln()
    }

    @Test
    fun repeatTestRunSNode2() {
        repeat(300) {
            testRunSNode2()
        }
    }

    @Test
    fun testRunSNode3() {
        "-".repeat(80).toPrintln()
        val globalTimeId = Res.globalTimeId
        val scxmlTuple = TrafficTestHelper.getSCXMLTuple()
        val executor = scxmlTuple.executor
        val mySCXML = scxmlTuple.dataSCXML

        fun strategyFireEvent() {
            val dataT: Int = mySCXML.getDataInt("T")!!
            val globalTime: Int = mySCXML.getDataInt(globalTimeId)!!
            var ifFired = false
            arrayListOf(
                SUnit("Aalborg", globalTime, 3..60, linkedMapOf("bike" to 1.0)),
                SUnit("Bike", dataT, 42..45, linkedMapOf("bike_end" to 1.0)),
                SUnit("Aalborg", globalTime, 0..2, linkedMapOf("train" to 1.0)),
                SUnit("Train", dataT, 4..6, linkedMapOf("train_go" to 10.0, "train_wait" to 1.0)),
                SUnit("Go", dataT, 35, linkedMapOf("train_go_end" to 1.0)),
                SUnit("Wait", globalTime, 0..2, linkedMapOf("train_wait_train" to 1.0)),
                SUnit("Wait", globalTime, 3..60, linkedMapOf("train_wait_back" to 1.0)),
                SUnit("GoBack", dataT, 0, linkedMapOf("back_back" to 1.0)),
            ).map {
                if (!ifFired) {
                    ifFired = it.tryFire(scxmlTuple, {}, ::strategyFireEvent) != null
                }
            }
        }

        executor.go()

        scxmlTuple.statusPrintln()
        while (true) {
            strategyFireEvent()
            //如果激活状态改变，则需要重新查看是否有策略可以发动
            if (mySCXML.ifDataExprEqualInt(globalTimeId, 60)) break
            if (executor.isInState("Sydney")) {
                break
            }
            mySCXML.setDataExprAddOne("T")
            mySCXML.setDataExprAddOne(globalTimeId)
        }
        scxmlTuple.statusPrintln()
    }

    @Test
    fun repeatTestRunSNode3() {
        repeat(1100) {
            testRunSNode3()
        }
    }

    @Test
    fun testRunSNode4_random() {
        "-".repeat(80).toPrintln()
        val globalTimeId = Res.globalTimeId
        val scxmlTuple = TrafficTestHelper.getSCXMLTuple()
        val executor = scxmlTuple.executor
        val mySCXML = scxmlTuple.dataSCXML

        fun strategyFireEvent() {
            val dataT: Int = mySCXML.getDataInt("T")!!
            var ifFired = false
            arrayListOf(
                SUnit("Bike", dataT, 42..45, linkedMapOf("bike_end" to 1.0)),
                SUnit("Car", dataT, 0, linkedMapOf("car_easy" to 10.0, "car_heavy" to 1.0)),
                SUnit("Easy", dataT, 20, linkedMapOf("car_easy_end" to 1.0)),
                SUnit("Heavy", dataT, 140, linkedMapOf("car_heavy_end" to 1.0)),
                SUnit("Train", dataT, 4..6, linkedMapOf("train_go" to 10.0, "train_wait" to 1.0)),
                SUnit("Go", dataT, 35, linkedMapOf("train_go_end" to 1.0)),
                SUnit("GoBack", dataT, 0, linkedMapOf("back_back" to 1.0)),
                SUnit("Aalborg", dataT, 0..2, linkedMapOf("bike" to 1.0, "car" to 1.0, "train" to 1.0)),
                SUnit("Wait", dataT, 0..2, linkedMapOf("train_wait_train" to 1.0, "train_wait_back" to 1.0)),
            ).map {
                if (!ifFired) {
                    ifFired = it.tryFire(scxmlTuple, {}, ::strategyFireEvent) != null
                }
            }
        }

        executor.go()

        scxmlTuple.statusPrintln()
        while (true) {
            strategyFireEvent()
            //如果激活状态改变，则需要重新查看是否有策略可以发动
            if (mySCXML.ifDataExprEqualInt(globalTimeId, 60)) break
            if (executor.isInState("Sydney")) {
                break
            }
            mySCXML.setDataExprAddOne("T")
            mySCXML.setDataExprAddOne(globalTimeId)
        }
        scxmlTuple.statusPrintln()
    }

    @Test
    fun repeatTestRunSNode4() {
        repeat(1100) {
            testRunSNode4_random()
        }
    }

    @Test
    fun testRunSNode5() {
        // 状态
        // 已经重试次数retry
        // 选取的event
        // 统计价值,剩余时间
        val a4LHM = LHMHelper.A4LHM<String, Int, String, Int>()
        //具体运行时，需要记录每个状态的上次被进入的全局时间
        arrayOf("Aalborg", "Wait").map { i1 ->
            (0..6).map { i2 ->
                when (i1) {
                    "Aalborg" -> {
                        arrayOf("car", "bike", "train").map { i3 ->
                            a4LHM.add(i1, i2, i3, 0)
                        }
                    }
                    "Wait" -> {
                        arrayOf("train_wait_train", "train_wait_back").map { i3 ->
                            a4LHM.add(i1, i2, i3, 0)
                        }
                    }
                    else -> {
                    }
                }
            }
        }

        data class SIISUnit(
            val stateId: String,
            val gTime: Int,
            val retry: Int,
            val event: String,
        )

        fun taskRunSNode4_random() {
            "--taskRunSNode4_random${"-".repeat(80)}".toPrintln()
            val globalTimeId = Res.globalTimeId
            val scxmlTuple = TrafficTestHelper.getSCXMLTuple()
            val executor = scxmlTuple.executor
            val mySCXML = scxmlTuple.dataSCXML

            val arrayList = ArrayList<SIISUnit>()

            //具体运行时，需要记录每个状态的上次被进入的全局时间
            val stateEnterTimeLHM = LinkedHashMap<String, Int>()
            stateEnterTimeLHM["Aalborg"] = 0

            val doOnEntry = { it: TransitionTarget ->
                stateEnterTimeLHM[it.id] = mySCXML.getDataInt(globalTimeId)!!
            }

            fun strategyFireEvent() {
                val dataT: Int = mySCXML.getDataInt("T")!!
                val dataRetry: Int = mySCXML.getDataInt("retryTrainCount")!!
                var ifFired = false
                arrayListOf(
                    SUnit(
                        "Aalborg", dataT, 0..2, linkedMapOf(
                            "bike" to 1.0,
                            "car" to 1.0, "train" to 1.0,
                        )
                    ),
                    SUnit("Bike", dataT, 42..45, linkedMapOf("bike_end" to 1.0)),
                    SUnit(
                        "Car", dataT, 0, linkedMapOf(
                            "car_easy" to 10.0,
                            "car_heavy" to 1.0,
                        )
                    ),
                    SUnit("Easy", dataT, 20, linkedMapOf("car_easy_end" to 1.0)),
                    SUnit("Heavy", dataT, 140, linkedMapOf("car_heavy_end" to 1.0)),
                    SUnit(
                        "Train", dataT, 4..6, linkedMapOf(
                            "train_go" to 10.0,
                            "train_wait" to 1.0,
                        )
                    ),
                    SUnit("Go", dataT, 35, linkedMapOf("train_go_end" to 1.0)),
                    SUnit(
                        "Wait", dataT, 0..2, linkedMapOf(
                            "train_wait_train" to 1.0,
                            "train_wait_back" to 1.0,
                        )
                    ),
                    SUnit("GoBack", dataT, 0, linkedMapOf("back_back" to 1.0)),
                ).map {
                    if (!ifFired) {
                        val event = it.tryFire(scxmlTuple, doOnEntry, ::strategyFireEvent)
                        ifFired = event != null
                        if (ifFired) {
                            if (it.stateId == "Aalborg" || it.stateId == "Wait") {
                                arrayList.add(
                                    SIISUnit(
                                        it.stateId,
                                        stateEnterTimeLHM[it.stateId]!!,
                                        dataRetry,
                                        event!!,
                                    )
                                )
                            }
                        }
                    }
                }
            }

            executor.go()

            scxmlTuple.statusPrintln()
            while (true) {
                strategyFireEvent()
                if (executor.isInState("Sydney")) {
                    val leftTime = 60 - mySCXML.getDataInt(globalTimeId)!!
                    arrayList.map {
                        println(it)
                        a4LHM[it.stateId]!![it.retry]!![it.event] =
                            a4LHM[it.stateId]!![it.retry]!![it.event]!! + leftTime
                    }
                    break
                }
                if (mySCXML.ifDataExprEqualInt(globalTimeId, 60)) {
                    val leftTime = -600
                    arrayList.map {
                        println(it)
                        a4LHM[it.stateId]!![it.retry]!![it.event] =
                            a4LHM[it.stateId]!![it.retry]!![it.event]!! + leftTime
                    }
                    break
                }
                mySCXML.setDataExprAddOne("T")
                mySCXML.setDataExprAddOne(globalTimeId)
            }
            scxmlTuple.statusPrintln()
        }

        repeat(10000) {
            taskRunSNode4_random()
        }

        a4LHM.touch { a1, a2, a3, a4 ->
            if (a4 != 0) {
                println("lhm[${a1}][${a2}][${a3}]=${a4}")
            }
        }
    }
}
