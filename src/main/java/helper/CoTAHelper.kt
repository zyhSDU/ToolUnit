package helper

import helper.base.StringHelper

object CoTAHelper {
    data class TimedAutomataSystem(
        val timedAutomataArrayList: ArrayList<TimedAutomata> = ArrayList(),
        val cp: ChangingParameter = ChangingParameter(),
        val runWhenPerTimeEnd: (Double) -> Unit = { _ -> },
    ) {
        @Suppress("NAME_SHADOWING")
        fun run(title: String, maxTimes: Int = 100) {
            println(StringHelper.titleString("-", 80, title))
            runClockAndCP(-cp.perRate, timedAutomataArrayList)
            while (cp.countTimes < maxTimes) {
                println(StringHelper.titleString("-", 40, "时钟步${cp.countTimes}"))

                var filter = timedAutomataArrayList.filter { it.ifCanRun() }
                if (filter.isEmpty()) break
                runClockAndCP(cp.perRate, filter)
                filter.map {
                    it.printNowStateAndCP()
                }

                filter = filter.filter { it.ifCanRun() }
                if (filter.isEmpty()) break
                filter.map {
                    it.runEdgeIfCan()
                }

                if (filter.isEmpty()) break

                runWhenPerTimeEnd(cp.energys[0].value)
                cp.countTimes++
            }
        }

        fun runClockAndCP(clockStep: Double, filter: List<TimedAutomata>) {
            cp.run(clockStep, filter)
            filter.map {
                it.runClock(clockStep)
            }
        }

        object Test {
            @JvmStatic
            fun main(args: Array<String>) {
                Test1.main(args)
                Test2OilPumpSystem.main(args)
            }
        }

        object Test1 {
            @JvmStatic
            fun main(args: Array<String>) {
                createTestTimedAutomataSystemVer1().run("测试1", 100)
                createTestTimedAutomataSystemVer2().run("测试2", 100)
            }

            fun createTestTimedAutomataSystemVer1(): TimedAutomataSystem {
                val cp = ChangingParameter(
                    arrayListOf(Energy("oil", 10.0)),
                )
                val t1 = createTestTimedAutomata1(cp, "机0")
                return TimedAutomataSystem(arrayListOf(t1), cp)
            }

            fun createTestTimedAutomataSystemVer2(): TimedAutomataSystem {
                val cp = ChangingParameter(
                    arrayListOf(Energy("oil", 10.0)),
                )
                val t1 = createTestTimedAutomata1(cp, "机0")
                val t2 = createTestTimedAutomata1(cp, "机1")
                return TimedAutomataSystem(arrayListOf(t1, t2), cp)
            }

            fun createTestTimedAutomata1(cp: ChangingParameter, name: String): TimedAutomata {
                val x = Clock("x")
                val y = Clock("y")
                val s0 = State("s0", {
                    y.value <= 1
                }, {
                    cp.energys[0].value += ((2.0) * it)
                })
                val s1 = State("s1", {
                    y.value <= 1
                }, {
                    cp.energys[0].value += ((4.0) * it)
                })
                val s2 = State("s2")
                val e0 = Edge(s0, s1, { y.value >= 0.25 }, {
                    cp.energys[0].value += (-3.0)
                    y.value = 0.0
                })
                val e1 = Edge(s1, s2, { x.value == 1.0 }, {
                    cp.energys[0].value += (0.0)
                    x.value = 0.0
                    y.value = 0.0
                })
                return TimedAutomata(
                    name,
                    s0,
                    arrayListOf(e0, e1),
                    cp,
                    arrayListOf(x, y),
                    ifEndState = { it == s2 },
                )
            }
        }

        object Test2OilPumpSystem {
            @JvmStatic
            fun main(args: Array<String>) {
                createTestTimedAutomataSystem().run("测试", 20)
            }

            fun createTestTimedAutomataSystem(): TimedAutomataSystem {
                var energyMax = Double.MIN_VALUE
                var energyMin = Double.MAX_VALUE
                val cp = ChangingParameter(
                    arrayListOf(Energy("oil", 0.0)),
                    2.0,
                )
                val t1 = createTestTimedAutomata1AccumulatorMachine(cp)
                val t2 = createTestTimedAutomata2AccumulatorPump(cp)
                return TimedAutomataSystem(arrayListOf(t1, t2), cp, runWhenPerTimeEnd = { energy ->
                    if (energy > energyMax) energyMax = energy
                    if (energy < energyMin) energyMin = energy
                    println("energyMin=${energyMin},energyMax=${energyMax},")
                })
            }

            fun createTestTimedAutomata1AccumulatorMachine(cp: ChangingParameter): TimedAutomata {
                val x = Clock("x")
                val s = arrayListOf(0.0, -1.2, 0.0, 0.0, -1.2, -2.5, 0.0, -1.7, -0.5, 0.0).withIndex()
                    .map { (i, energyPerRate) ->
                        State("s${i}", {
                            x.value <= 2
                        }, {
                            cp.energys[0].value += ((energyPerRate) * it)
                        })
                    } as ArrayList<State>
                val size = s.size
                val e = (0 until size).map {
                    Edge(s[it], s[(it + 1) % size], {
                        x.value == 2.0
                    }, {
                        x.value = 0.0
                    })
                } as ArrayList<Edge>

                return TimedAutomata("机器", s[0], e, cp, arrayListOf(x))
            }

            @Suppress("DuplicatedCode")
            fun createTestTimedAutomata2AccumulatorPump(cp: ChangingParameter): TimedAutomata {
                val timedAutomata: TimedAutomata
                val solution = arrayListOf(1, 1, 1, 1, 0, 0, 0, 0, 0, 0)
                val ifOn = {
                    solution[(cp.countTimes) % solution.size] == 1
                }
                val x = Clock("x", 2.0)
                val s0 = State("s0", { x.value <= 2 })
                val s1 = State("s1", { x.value <= 2 }, { cp.energys[0].value += ((2.2) * it) })
                val e: ArrayList<Edge>
                val e00 = Edge(s0, s0, { x.value == 2.0 && !ifOn() }, { x.value = 0.0 })
                val e01 = Edge(s0, s1, { x.value == 2.0 && ifOn() }, { x.value = 0.0 })
                val e10 = Edge(s1, s0, { x.value == 2.0 && !ifOn() }, { x.value = 0.0 })
                val e11 = Edge(s1, s1, { x.value == 2.0 && ifOn() }, { x.value = 0.0 })
                e = arrayListOf(e00, e01, e10, e11)
                timedAutomata = TimedAutomata("油泵", s0, e, cp, arrayListOf(x))
                return timedAutomata
            }
        }
    }

    class KripkeStruct(
        val state: State,
        val clockValues: ArrayList<Double>,
        val energyValues: ArrayList<Double>,
    )

    data class TimedAutomata(
        val name: String,
        val s0: State,
        val e: ArrayList<Edge> = ArrayList(),
        val cp: ChangingParameter = ChangingParameter(),
        val clockArrayList: ArrayList<Clock> = ArrayList(),//时钟值们
        val ifError: (State) -> Boolean = {
            !it.isStateNoError() //目前只考虑了状态错误
        },
        val ifEndState: (State) -> Boolean = {
            false
        },
        var nowState: State = s0,
    ) {

        fun ifCanRun(): Boolean {
            if (ifError(nowState)) {
                println("${name}状态错误：错误状态为${nowState.name}")
            }
            if (ifEndState(nowState)) {
                println("${name}状态终结：终结状态为${nowState.name}")
            }
            return !ifError(nowState) && !ifEndState(nowState)
        }

        init {
            modifyNowState(s0)
        }

        fun printNowStateAndCP() {
            val stringBuilder = StringBuilder().apply {
                append("${name}：")
                append("（状态，时钟，能量，）=（")

                append("（")
                append("${nowState.name}\t")
                append("），")

                append("（")
                clockArrayList.map {
                    it.value
                }.map {
                    append("${it}，\t")
                }
                append("），")

                append("（")
                cp.energys.map {
                    it.value
                }.map {
                    append("${it}，\t")
                }
                append("），")

                append("）")
            }
            println(stringBuilder.toString())
        }

        fun runEdgeIfCan() {
            //检查
            val list = e.filter {
                it.node0 == nowState
            }.filter {
                it.ifEdgeCanRun()
            }
            //发动
            if (list.isNotEmpty()) {
                //如果多条边都能发动，如何取舍？目前取第0个
                //当前，边一旦符合，立即发动，不留滞
                //连续发动边，咋整？
                list[0].doSomeIfEdgeRun()
                modifyNowState(list[0].node1)
                printNowStateAndCP()
                if (ifEndState(nowState)) {
                    println("${name}，状态终结，为${nowState.name}")
                }
            }
        }

        fun modifyNowState(state: State) {
            nowState = state
        }

        fun runClock(clockStep: Double) {
            clockArrayList.map {
                it.run(clockStep)
            }
        }
    }

    data class State(
        val name: String = "null",
        //这里需要，验时钟，验能量
        val isStateNoError: () -> Boolean = { true },
        //这里填入时钟步，抽象成函数，方便加入扰动
        val runPerRate: (Double) -> Unit = {},
    )

    data class Energy(
        val name: String,
        var value: Double = 0.0,
    ) : Cloneable {
        fun run(changeRate: Double, clockStep: Double) {
            value += (changeRate * clockStep)
        }

        public override fun clone(): Energy {
            return Energy(name, value)
        }
    }

    data class Clock(
        val name: String,
        var value: Double = 0.0,
    ) : Cloneable {
        fun run(clockStep: Double) {
            value += clockStep
        }

        public override fun clone(): Clock {
            return Clock(name, value)
        }
    }

    data class ChangingParameter(
        val energys: ArrayList<Energy> = ArrayList(),//能量们，或者说，连续资源们//放在这里合理吗
        var perRate: Double = 0.25,//放在这里合理吗
        var countTimes: Int = 0,//放在这里合理吗
    ) : Cloneable {
        fun run(clockStep: Double, timedAutomataArrayList: List<TimedAutomata>) {
            runEnergy(clockStep, timedAutomataArrayList)
        }

        fun runEnergy(clockStep: Double, timedAutomataArrayList: List<TimedAutomata>) {
            //能量虽然是共有的，但是需要所有在运行的自动机同时来作用
            //而如果时钟是共有的，则单独就能运行
            timedAutomataArrayList.map {
                it.nowState.runPerRate(clockStep)
            }
        }

        public override fun clone(): ChangingParameter {
            val energy2 = ArrayList<Energy>().apply {
                energys.map {
                    add(it.clone())
                }
            }
            return ChangingParameter(
                energy2,
                perRate,
                countTimes,
            )
        }
    }

    class Edge(
        val node0: State,
        val node1: State,
        val ifEdgeCanRun: () -> Boolean = { true },
        val doSomeIfEdgeRun: () -> Unit = {},
    )
}