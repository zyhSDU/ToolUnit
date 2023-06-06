package uppaal_test

import helper.base.ZoneHelper
import org.junit.Test
import uppaal_test.ZoneTest.StateEventPLHM.Expand.toP1StateEventPLHM

internal class ZoneTest {
    data class ZoneState(
        val name: String,
        val location: String,
        val cons: String,
        val reward: Double = 0.0,
    )

    data class ZoneTransition(
        override val start: ZoneState,
        override val event: String,
        override val end: ZoneState,
    ) : ZoneHelper.ZoneTransition<ZoneState>(
        start,
        event,
        end,
    ) {
        fun clone(): ZoneTransition {
            return this.copy()
        }
    }

    class StateEventTransitionLHM :
        ZoneHelper.StateEventTransitionLHM<
                ZoneState,
                ZoneTransition,
                >(
        ) {
        fun touchPrintlnType1(
            sb: StringBuilder = StringBuilder(),
        ): String {
            this.touch { a1, a2, a3 ->
                sb.append("${a1.name},${a2},${a3.end.name}\n")
            }
            return sb.toString()
        }
    }

    class StateEventPLHM : ZoneHelper.StateEventPLHM<ZoneState>() {
        fun touchPrintlnType1(
            sb: StringBuilder = StringBuilder(),
        ): StringBuilder {
            this.touch { a1, a2, a3 ->
                sb.append("${a1.name},${a2},${a3}\n")
            }
            return sb
        }

        object Expand {
            fun StateEventTransitionLHM.toP1StateEventPLHM(): StateEventPLHM {
                return StateEventPLHM().also {
                    this.touch { zoneState, s, _ ->
                        it.add(zoneState, s, 1.0)
                    }
                }
            }
        }
    }

    class ZonePath(
        pReward: Double = 1.0,
    ) : ZoneHelper.ZonePath<ZoneTransition>(pReward) {
        override fun clone(): ZonePath {
            val newPath = ZonePath(this.pReward)
            this.map {
                newPath.add(it.clone())
            }
            return newPath
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("ZonePath(")
            this.map {
                sb.append("(${it.start.name},${it.event},${it.end.name})")
                sb.append(",")
            }
            sb.append("pReward=${pReward}")
            sb.append(")")
            return sb.toString()
        }
    }

    /**
     * 不能有环路，人工判断
     */
    data class ZoneGraph(
        val start: ZoneState,
        val ts: ArrayList<ZoneTransition>,
        val finalStates: LinkedHashSet<ZoneState> = LinkedHashSet(),
    ) {
        val stateLHS = LinkedHashSet<ZoneState>()
        val stateEventTLHM = StateEventTransitionLHM()
        var stateEventPLHM = StateEventPLHM()

        init {
            ts.map {
                arrayOf(it.start, it.end).map {
                    stateLHS.add(it)
                }
            }
            ts.map {
                stateEventTLHM.add(it.start, it.event, it)
            }
            stateEventPLHM = stateEventTLHM.toP1StateEventPLHM()
        }

        fun dfsTouchType1(
            nowZT: ZoneTransition? = null,
            tabSize: Int = 0,
            visited: LinkedHashSet<ZoneTransition> = LinkedHashSet(),
            sb: StringBuilder = StringBuilder(),
        ): String {
            val isNowZTNull = nowZT == null
            if (isNowZTNull) {
                sb.append("${start.name}\n")
            } else {
                nowZT!!.let {
                    sb.append("${"\t".repeat(tabSize)}${it.event} ${it.end.name}\n")
                }
            }
            val nowState: ZoneState = if (isNowZTNull) {
                start
            } else {
                nowZT!!.end
            }
            if (!isNowZTNull) {
                visited.add(nowZT!!)
            }
            stateEventTLHM[nowState]?.map { (_, a3) ->
                if (visited.contains(a3)) return@map
                dfsTouchType1(
                    a3,
                    tabSize + 1,
                    visited,
                    sb,
                )
            }
            return sb.toString()
        }

        fun dfsTouchType2(
            nowZT: ZoneTransition? = null,
            visited: LinkedHashSet<ZoneTransition> = LinkedHashSet(),
            nowPath: ZonePath = ZonePath(),
            pathList: ArrayList<ZonePath> = ArrayList(),
        ) {
            val isNowZoneTransitionNull = nowZT == null
            if (!isNowZoneTransitionNull) {
                nowZT!!
                val totalP = this.stateEventPLHM[nowZT.start]!!.values.sum()
                val nowP = this.stateEventPLHM[nowZT.start]!![nowZT.event]!!
                nowPath.add(nowZT, nowP / totalP)
            }
            val nowState: ZoneState = if (isNowZoneTransitionNull) {
                start
            } else {
                nowZT!!.end
            }
            val isEnd = this.finalStates.contains(nowState)
            if (isEnd) {
                nowPath.pReward *= nowState.reward
                pathList.add(nowPath)
                return
            }
            if (!isNowZoneTransitionNull) {
                visited.add(nowZT!!)
            }
            stateEventTLHM[nowState]?.map { (_, a3) ->
                if (visited.contains(a3)) return@map
                val newPath = nowPath.clone()
                dfsTouchType2(
                    a3,
                    visited,
                    newPath,
                    pathList,
                )
            }
        }
    }

    private fun getZoneGraphObj1(): ZoneGraph {
        val zone1 = ZoneState("z1", "l0", "x=y")
        val zone2 = ZoneState("z2", "l1", "0<=x<1 and x<=y")
        val zone3 = ZoneState("z3", "l2", "0<=x<1 and 0<=y")
        val zone4 = ZoneState("z4", "l3", "0<=x and 0<=y")
        val zone5 = ZoneState("z5", "l0", "2<x and 2<y")

        val ts = arrayListOf(
            ZoneTransition(zone1, "t12", zone2),
            ZoneTransition(zone2, "t23", zone3),
            ZoneTransition(zone3, "t34", zone4),
            ZoneTransition(zone4, "t44", zone4),
            ZoneTransition(zone4, "t45", zone5),
            ZoneTransition(zone5, "t52", zone2),
        )

        return ZoneGraph(zone1, ts)
    }

    @Test
    fun t1t1() {
        val zoneGraph = getZoneGraphObj1()
        val rs = zoneGraph.stateEventTLHM.touchPrintlnType1()
        println(rs)
        assert(
            rs.trim() == """
z1,t12,z2
z2,t23,z3
z3,t34,z4
z4,t44,z4
z4,t45,z5
z5,t52,z2
""".trim()
        )
    }

    @Test
    fun t1t2() {
        val zoneGraph = getZoneGraphObj1()
        val rs = zoneGraph.dfsTouchType1()
        println(rs)
        assert(
            rs.trim() == """
z1
	t12 z2
		t23 z3
			t34 z4
				t44 z4
					t45 z5
						t52 z2
""".trim()
        )
    }

    private fun getZoneGraphObj2(): ZoneGraph {
        val zone1 = ZoneState("z1", "l0", "x=y")
        val zone2 = ZoneState("z2", "l1", "0<=x<1 and x<=y")
        val zone3 = ZoneState("z3", "l2", "0<=x<1 and 0<=y")
        val zone4 = ZoneState("z4", "l3", "0<=x and 0<=y")
        val zone5 = ZoneState("z5", "l0", "2<x and 2<y")

        val ts = arrayListOf(
            ZoneTransition(zone1, "t12", zone2),
            ZoneTransition(zone2, "t23", zone3),
            ZoneTransition(zone2, "t24", zone4),
            ZoneTransition(zone3, "t34", zone4),
            ZoneTransition(zone4, "t44", zone4),
            ZoneTransition(zone4, "t45", zone5),
        )

        return ZoneGraph(zone1, ts)
    }

    @Test
    fun t2t1() {
        val zoneGraph = getZoneGraphObj2()
        val rs = zoneGraph.dfsTouchType1()
        println(rs)
        assert(
            rs.trim() == """
z1
	t12 z2
		t23 z3
			t34 z4
				t44 z4
					t45 z5
		t24 z4
        """.trimIndent()
        )
    }

    //不能有环路
    private fun getZoneGraphObj3(): ZoneGraph {
        val zone1 = ZoneState("z1", "l0", "x=y")
        val zone2 = ZoneState("z2", "l1", "0<=x<1 and x<=y")
        val zone3 = ZoneState("z3", "l2", "0<=x<1 and 0<=y")
        val zone4 = ZoneState("z4", "l3", "0<=x and 0<=y", 60.0)
        val zone5 = ZoneState("z5", "l0", "2<x and 2<y", 50.0)

        val ts = arrayListOf(
            ZoneTransition(zone1, "t12", zone2),
            ZoneTransition(zone2, "t23", zone3),
            ZoneTransition(zone2, "t24", zone4),
            ZoneTransition(zone2, "t25", zone5),
            ZoneTransition(zone3, "t34", zone4),
        )

        return ZoneGraph(
            zone1,
            ts,
            finalStates = linkedSetOf(
                zone4,
                zone5,
            ),
        )
    }

    @Test
    fun t3t1() {
        val zoneGraph = getZoneGraphObj3()
        val rs1 = zoneGraph.stateEventTLHM.touchPrintlnType1()
        val rs2 = zoneGraph.stateEventPLHM.touchPrintlnType1()
        val pathList: ArrayList<ZonePath> = ArrayList()
        zoneGraph.dfsTouchType2(
            pathList = pathList,
        )
        val sb3 = StringBuilder()
        pathList.map {
            sb3.append("$it\n")
        }
        val rs3 = sb3.toString()
        println(rs1)
        println(rs2)
        println(rs3)

        assert(
            rs1.trim() == """
z1,t12,z2
z2,t23,z3
z2,t24,z4
z2,t25,z5
z3,t34,z4
""".trim()
        )
        assert(
            rs2.trim() == """
z1,t12,1.0
z2,t23,1.0
z2,t24,1.0
z2,t25,1.0
z3,t34,1.0
""".trim()
        )
        assert(
            rs3.trim() == """
ZonePath((z1,t12,z2),(z2,t23,z3),(z3,t34,z4),pReward=20.0)
ZonePath((z1,t12,z2),(z2,t24,z4),pReward=20.0)
ZonePath((z1,t12,z2),(z2,t25,z5),pReward=16.666666666666664)
""".trim()
        )
    }

    @Test
    fun t3t2() {
        val zoneGraph = getZoneGraphObj3().also { it ->
            val zone2 = it.stateLHS.toArray()[1]!!
            it.stateEventPLHM.let {
                //概率生效
                it[zone2]!!["t24"] = 2.0
                it[zone2]!!["t25"] = 2.0
            }
        }
        val rs1 = zoneGraph.stateEventTLHM.touchPrintlnType1()
        val rs2 = zoneGraph.stateEventPLHM.touchPrintlnType1()
        val pathList: ArrayList<ZonePath> = ArrayList()
        zoneGraph.dfsTouchType2(
            pathList = pathList,
        )
        val sb3 = StringBuilder()
        pathList.map {
            sb3.append("$it\n")
        }
        val rs3 = sb3.toString()
        println(rs1)
        println(rs2)
        println(rs3)

        assert(
            rs1.trim() == """
z1,t12,z2
z2,t23,z3
z2,t24,z4
z2,t25,z5
z3,t34,z4
""".trim()
        )
        assert(
            rs2.trim() == """
z1,t12,1.0
z2,t23,1.0
z2,t24,2.0
z2,t25,2.0
z3,t34,1.0
""".trim()
        )
        assert(
            rs3.trim() == """
ZonePath((z1,t12,z2),(z2,t23,z3),(z3,t34,z4),pReward=12.0)
ZonePath((z1,t12,z2),(z2,t24,z4),pReward=24.0)
ZonePath((z1,t12,z2),(z2,t25,z5),pReward=20.0)
""".trim()
        )
    }

    @Test
    fun t4t1() {
        val zoneGraph = getZoneGraphObj3()
        val sb1 = StringBuilder()
        zoneGraph.stateLHS.map {
            sb1.append("$it\n")
            sb1.append("${zoneGraph.stateLHS.indexOf(it)}\n")
        }
        val rs1 = sb1.toString()
        println(rs1)
        assert(
            rs1.trim() == """
ZoneState(name=z1, location=l0, cons=x=y, reward=0.0)
0
ZoneState(name=z2, location=l1, cons=0<=x<1 and x<=y, reward=0.0)
1
ZoneState(name=z3, location=l2, cons=0<=x<1 and 0<=y, reward=0.0)
2
ZoneState(name=z4, location=l3, cons=0<=x and 0<=y, reward=60.0)
3
ZoneState(name=z5, location=l0, cons=2<x and 2<y, reward=50.0)
4
        """.trimIndent().trim()
        )
    }
}