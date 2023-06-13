package helper.scxml.scxml2.t7_cycle

import helper.base.LHMHelper.LHMExpand.add
import helper.base.MathHelper
import helper.scxml.scxml2.*
import helper.scxml.scxml2.EnvHelper
import helper.scxml.scxml2.t7_cycle.EnvHelper.Env.Expand.ToClockValuations.toClockValuations

object EnvHelper {
    class Env(
        override val strategyTuple: StrategyTripleHelper.Type2StrategyTuple,
        private val machineTimeMax: Int,
    ) : EnvHelper.T3BaseEnv() {
        object Expand {
            object ToClockValuations {
                fun LinkedHashMap<String, String>.toClockValuations(
                ): helper.scxml.scxml2.MathHelper.ClockValuations {
                    val v = helper.scxml.scxml2.MathHelper.ClockValuations()
                    v.add(this[Res.globalTimeId]!!.toDouble())
                    v.add(this["x"]!!.toDouble())
                    return v
                }
            }

            fun ArrayList<EnvHelper.RunResult>.toLocationEventVListLHM(
            ): helper.scxml.scxml2.MathHelper.LocationEventVListLHM {
                val lhm = helper.scxml.scxml2.MathHelper.LocationEventVListLHM()
                this.map {
                    it.us.map {
                        lhm.add(it.location, it.action, helper.scxml.scxml2.MathHelper.ClockValuationsList())
                        lhm[it.location]!![it.action]!!.add(it.data.toClockValuations())
                    }
                }
                return lhm
            }
        }

        override val scxmlTuple = Scxml2Helper.getSCXMLTuple("scxml2/t7_cycle/cycle1.scxml").also {
            it.initialStateList.add("s0")
            it.renStateList.add("s1")
            it.finalStateList.add("s4")
            arrayListOf("s0", "s1", "s2", "s3").map { stateId ->
                it.stateNeedConsiderClockListLHM.add(stateId, arrayListOf("x"))
            }
            arrayListOf("s0", "s1", "s2", "s3").map { stateId ->
                it.stateDataIncrementLHM.add(stateId, "x", 1.0)
            }
            it.stateDataIncrementLHM.run {
                add("s0", "x", 1.0)
                add("s1", "c", 4.0)
                add("s2", "c", 3.0)
                add("s3", "c", 2.0)
            }
        }

        override val ifMachineTimeMax: Boolean
            get() {
                return dataGlobalTimeInt >= machineTimeMax
            }
    }

    fun getIEnvEventSelectorFunObj1(): (SCXMLTuple) -> StrategyTripleHelper.IEnvEventSelector {
        return { scxmlTuple ->
            object : StrategyTripleHelper.IEnvEventSelector {
                override fun getEvent(stateId: String): String? {
                    val dataXInt: Int = scxmlTuple.dataSCXML.getDataInt("x")!!
                    when (stateId) {
                        "s0" -> {
                            if (!EnvHelper.T3BaseEnv.ifCanNextWhenOneClock(dataXInt, 0..100)) return null
                            linkedMapOf("s0s1" to 1.0).let {
                                return MathHelper.getRandomString(it)
                            }
                        }
                        "s1" -> {
                            if (!EnvHelper.T3BaseEnv.ifCanNextWhenOneClock(dataXInt, 90 until 100)) return null
                            linkedMapOf("s1s4t2" to 1.0).let {
                                return MathHelper.getRandomString(it)
                            }
                        }
                        "s2" -> {
                            if (!EnvHelper.T3BaseEnv.ifCanNextWhenOneClock(dataXInt, 60..120)) return null
                            linkedMapOf("s2s4" to 1.0).let {
                                return MathHelper.getRandomString(it)
                            }
                        }
                        "s3" -> {
                            if (!EnvHelper.T3BaseEnv.ifCanNextWhenOneClock(dataXInt, 20..140)) return null
                            linkedMapOf("s3s4" to 1.0).let {
                                return MathHelper.getRandomString(it)
                            }
                        }
                    }
                    return null
                }
            }
        }
    }

    fun getIRenEventSelectorFunObj1(): (SCXMLTuple) -> StrategyTripleHelper.IRenEventSelector {
        return { scxmlTuple ->
            object : StrategyTripleHelper.IRenEventSelector {
                override fun getEvent(stateId: String): String? {
                    val dataXInt = scxmlTuple.dataSCXML.getDataInt("x")!!
                    when (stateId) {
                        "s1" -> {
                            when (dataXInt) {
                                100 -> {
                                    return "s1s4t1"
                                }
                                else -> {
                                    //为了均匀
                                    if (!EnvHelper.T3BaseEnv.ifCanNextWhenOneClock(dataXInt, 0 until 100)) return null
                                    linkedMapOf(
                                        "s1s2" to 1.0,
                                        "s1s3" to 1.0,
                                    ).let {
                                        return MathHelper.getRandomString(it)
                                    }
                                }
                            }
                        }
                    }
                    return null
                }
            }
        }
    }

    fun getEnvObj1(): Env {
        return Env(
            machineTimeMax = Int.MAX_VALUE,
            strategyTuple = StrategyTripleHelper.Type2StrategyTuple(
                getIEnvEventSelectorFun = getIEnvEventSelectorFunObj1(),
                getIRenEventSelectorFun = getIRenEventSelectorFunObj1(),
            ),
        )
    }

    fun getEnvObj2(): Env {
        return Env(
            machineTimeMax = 210,
            strategyTuple = StrategyTripleHelper.Type2StrategyTuple(
                getIEnvEventSelectorFun = getIEnvEventSelectorFunObj1(),
                getIRenEventSelectorFun = { scxmlTuple ->
                    object : StrategyTripleHelper.IRenEventSelector {
                        override fun getEvent(stateId: String): String? {
                            val dataXInt = scxmlTuple.dataSCXML.getDataInt("x")!!
                            when (stateId) {
                                "s1" -> {
                                    when (dataXInt) {
                                        100 -> {
                                            return "s1s4t1"
                                        }
                                    }
                                }
                            }
                            return null
                        }
                    }
                },
            ),
        )
    }

    fun getEnvObj3(): Env {
        return Env(
            machineTimeMax = 210,
            strategyTuple = StrategyTripleHelper.Type2StrategyTuple(
                getIEnvEventSelectorFun = getIEnvEventSelectorFunObj1(),
                getIRenEventSelectorFun = { scxmlTuple ->
                    object : StrategyTripleHelper.IRenEventSelector {
                        override fun getEvent(stateId: String): String? {
                            when (stateId) {
                                "s1" -> {
                                    val dataXInt = scxmlTuple.dataSCXML.getDataInt("x")!!
                                    val dataGInt = scxmlTuple.dataSCXML.getDataInt(Res.globalTimeId)!!
                                    //这里改为上次进入时间比较合适
                                    val enterS1Time = dataGInt - dataXInt
                                    when {
                                        enterS1Time > 90 -> {
                                            if (dataXInt == 100) {
                                                return "s1s4t1"
                                            }
                                        }
                                        enterS1Time > 70 && enterS1Time <= 90 -> {
                                            return "s1s2"
                                        }
                                        enterS1Time <= 70 -> {
                                            return "s1s3"
                                        }
                                    }
                                }
                            }
                            return null
                        }
                    }
                },
            ),
        )
    }
}
