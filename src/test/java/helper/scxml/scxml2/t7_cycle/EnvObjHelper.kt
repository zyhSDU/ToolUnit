package helper.scxml.scxml2.t7_cycle

import helper.base.MathHelper
import helper.scxml.scxml2.EnvHelper.T3BaseEnv.Companion.ifCanNextWhenOneClock
import helper.scxml.scxml2.Res
import helper.scxml.scxml2.SCXMLTuple
import helper.scxml.scxml2.StrategyTripleHelper
import helper.scxml.scxml2.StrategyTripleHelper.IRenEventSelector
import helper.scxml.scxml2.StrategyTripleHelper.Type2StrategyTuple
import helper.scxml.scxml2.t7_cycle.EnvHelper.Env

object EnvObjHelper {
    fun getEnvEventSelectorFunObj1(): (SCXMLTuple) -> StrategyTripleHelper.IEnvEventSelector {
        return { scxmlTuple ->
            object : StrategyTripleHelper.IEnvEventSelector {
                override fun getEvent(stateId: String): String? {
                    val dataXInt: Int = scxmlTuple.dataSCXML.getDataInt("x")!!
                    when (stateId) {
                        "s0" -> {
                            if (!ifCanNextWhenOneClock(dataXInt, 0..100)) return null
                            linkedMapOf("s0s1" to 1.0).let {
                                return MathHelper.getRandomString(it)
                            }
                        }
                        "s1" -> {
                            if (!ifCanNextWhenOneClock(dataXInt, 90 until 100)) return null
                            linkedMapOf("s1s4t2" to 1.0).let {
                                return MathHelper.getRandomString(it)
                            }
                        }
                        "s2" -> {
                            if (!ifCanNextWhenOneClock(dataXInt, 60..120)) return null
                            linkedMapOf("s2s4" to 1.0).let {
                                return MathHelper.getRandomString(it)
                            }
                        }
                        "s3" -> {
                            if (!ifCanNextWhenOneClock(dataXInt, 20..140)) return null
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

    fun getRenEventSelectorFunObj1(): (SCXMLTuple) -> IRenEventSelector {
        return { scxmlTuple ->
            object : IRenEventSelector {
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
                                    if (!ifCanNextWhenOneClock(dataXInt, 0 until 100)) return null
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
            strategyTuple = Type2StrategyTuple(
                getEnvEventSelectorFun = getEnvEventSelectorFunObj1(),
                getRenEventSelectorFun = getRenEventSelectorFunObj1(),
            ),
        )
    }

    fun getEnvObj2(): Env {
        return Env(
            machineTimeMax = 210,
            strategyTuple = Type2StrategyTuple(
                getEnvEventSelectorFun = getEnvEventSelectorFunObj1(),
                getRenEventSelectorFun = { scxmlTuple ->
                    object : IRenEventSelector {
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
            strategyTuple = Type2StrategyTuple(
                getEnvEventSelectorFun = getEnvEventSelectorFunObj1(),
                getRenEventSelectorFun = { scxmlTuple ->
                    object : IRenEventSelector {
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