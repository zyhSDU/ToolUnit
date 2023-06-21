package helper.scxml.scxml2.t6_zone

import helper.base.ConstraintHelper.CompareOp.Companion.eOp
import helper.base.ConstraintHelper.CompareOp.Companion.gOp
import helper.base.ConstraintHelper.CompareOp.Companion.geOp
import helper.base.ConstraintHelper.CompareOp.Companion.lOp
import helper.base.ConstraintHelper.CompareOp.Companion.leOp
import helper.base.ConstraintHelper.N1N2ConstraintHelper.getXConstraint
import helper.base.ConstraintHelper.N1N2ConstraintHelper.getXYConstraint
import helper.base.ConstraintHelper.N1N2ConstraintHelper.getYConstraint
import helper.scxml.scxml2.ZoneHelper.ZoneGraph
import helper.scxml.scxml2.ZoneHelper.ZonePath
import helper.scxml.scxml2.ZoneHelper.ZoneState
import helper.scxml.scxml2.ZoneHelper.ZoneState.Companion.getL1C1ZoneState
import helper.scxml.scxml2.ZoneHelper.ZoneState.Companion.getL1C2ZoneState
import helper.scxml.scxml2.ZoneHelper.ZoneTransition.Companion.getEmptyEventZoneTransition
import org.junit.Test

internal class TransitionHelperTest {
    private fun getZSList(): ArrayList<ZoneState> {
        val zone0 = getL1C1ZoneState(
            "l0",
            getXYConstraint(eOp, 0.0),
        )
        val zone1 = getL1C2ZoneState(
            "l1",
            getXConstraint(geOp, 0.0, lOp, 1.0),
            getXYConstraint(leOp, 0.0),
        )
        val zone2 = getL1C2ZoneState(
            "l2",
            getXConstraint(geOp, 0.0, lOp, 1.0),
            getYConstraint(geOp, 0.0),
        )
        val zone3 = getL1C2ZoneState(
            "l3",
            getXConstraint(geOp, 0.0),
            getYConstraint(geOp, 0.0),
        )
        val zone4 = getL1C2ZoneState(
            "l0",
            getXConstraint(gOp, 2.0),
            getYConstraint(gOp, 2.0),
        )
        return arrayListOf(zone0, zone1, zone2, zone3, zone4)
    }

    private fun getZoneGraphObj1(): ZoneGraph {
        val zsList = getZSList()

        val zone0 = zsList[0]
        val zone1 = zsList[1]
        val zone2 = zsList[2]
        val zone3 = zsList[3]
        val zone4 = zsList[4]

        val ts = arrayListOf(
            getEmptyEventZoneTransition(zone0, zone1),
            getEmptyEventZoneTransition(zone1, zone2),
            getEmptyEventZoneTransition(zone2, zone3),
            getEmptyEventZoneTransition(zone3, zone3),
            getEmptyEventZoneTransition(zone3, zone4),
            getEmptyEventZoneTransition(zone4, zone1),
        )

        return ZoneGraph(zone0, ts)
    }

    @Test
    fun t1t1() {
        val zoneGraph = getZoneGraphObj1()
        val rs = zoneGraph.stateEventTLHM.touchPrintlnType2().trim()
        println(rs)
        assert(
            rs == """
            ----------------------------------------
            [z0,(l0),(x-y=0.0)]
            [z0z1]
            [z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
            ----------------------------------------
            [z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
            [z1z2]
            [z2,(l2),(0.0<=x<1.0 and y>=0.0)]
            ----------------------------------------
            [z2,(l2),(0.0<=x<1.0 and y>=0.0)]
            [z2z3]
            [z3,(l3),(x>=0.0 and y>=0.0)]
            ----------------------------------------
            [z3,(l3),(x>=0.0 and y>=0.0)]
            [z3z3]
            [z3,(l3),(x>=0.0 and y>=0.0)]
            ----------------------------------------
            [z3,(l3),(x>=0.0 and y>=0.0)]
            [z3z4]
            [z4,(l0),(x>2.0 and y>2.0)]
            ----------------------------------------
            [z4,(l0),(x>2.0 and y>2.0)]
            [z4z1]
            [z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
        """.trimIndent()
        )
    }

    @Test
    fun t1t2() {
        val zoneGraph = getZoneGraphObj1()
        val rs = zoneGraph.dfsTouchType1().trim()
        println(rs)
        assert(
            rs == """
[z0,(l0),(x-y=0.0)]
	[z0z1][z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
		[z1z2][z2,(l2),(0.0<=x<1.0 and y>=0.0)]
			[z2z3][z3,(l3),(x>=0.0 and y>=0.0)]
				[z3z3][z3,(l3),(x>=0.0 and y>=0.0)]
					[z3z4][z4,(l0),(x>2.0 and y>2.0)]
						[z4z1][z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
        """.trimIndent()
        )
    }

    private fun getZoneGraphObj2(): ZoneGraph {
        val zsList = getZSList()

        val zone0 = zsList[0]
        val zone1 = zsList[1]
        val zone2 = zsList[2]
        val zone3 = zsList[3]
        val zone4 = zsList[4]

        val ts = arrayListOf(
            getEmptyEventZoneTransition(zone0, zone1),
            getEmptyEventZoneTransition(zone1, zone2),
            getEmptyEventZoneTransition(zone1, zone3),
            getEmptyEventZoneTransition(zone2, zone3),
            getEmptyEventZoneTransition(zone3, zone3),
            getEmptyEventZoneTransition(zone3, zone4),
        )

        return ZoneGraph(zone0, ts)
    }

    @Test
    fun t2t1() {
        val zoneGraph = getZoneGraphObj2()
        val rs = zoneGraph.dfsTouchType1().trim()
        println(rs)
        assert(
            rs == """
[z0,(l0),(x-y=0.0)]
	[z0z1][z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
		[z1z2][z2,(l2),(0.0<=x<1.0 and y>=0.0)]
			[z2z3][z3,(l3),(x>=0.0 and y>=0.0)]
				[z3z3][z3,(l3),(x>=0.0 and y>=0.0)]
					[z3z4][z4,(l0),(x>2.0 and y>2.0)]
		[z1z3][z3,(l3),(x>=0.0 and y>=0.0)]
        """.trimIndent()
        )
    }

    //不能有环路
    private fun getZoneGraphObj3(): ZoneGraph {
        val zsList = getZSList()

        val zone0 = zsList[0]
        val zone1 = zsList[1]
        val zone2 = zsList[2]
        val zone3 = zsList[3]
        val zone4 = zsList[4]

        zone3.reward = 60.0
        zone4.reward = 50.0

        val ts = arrayListOf(
            getEmptyEventZoneTransition(zone0, zone1),
            getEmptyEventZoneTransition(zone1, zone2),
            getEmptyEventZoneTransition(zone1, zone3),
            getEmptyEventZoneTransition(zone1, zone4),
            getEmptyEventZoneTransition(zone2, zone3),
        )

        return ZoneGraph(
            zone0,
            ts,
            finalStates = linkedSetOf(
                zone3,
                zone4,
            ),
        )
    }

    @Test
    fun t3t1() {
        val zoneGraph = getZoneGraphObj3()
        val rs1 = zoneGraph.stateEventTLHM.touchPrintlnType2()
        val rs2 = zoneGraph.stateEventPLHM.touchPrintlnType1()
        val pathList: ArrayList<ZonePath> = ArrayList()
        zoneGraph.dfsTouchType2(
            pathList = pathList,
        )
        val sb3 = StringBuilder()
        pathList.map {
            sb3.append("$it\n")
        }
        val rs3 = sb3.toString().trim()
        println(rs1)
        println(rs2)
        println(rs3)
        assert(
            rs1.trim() == """
            ----------------------------------------
            [z0,(l0),(x-y=0.0)]
            [z0z1]
            [z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
            ----------------------------------------
            [z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
            [z1z2]
            [z2,(l2),(0.0<=x<1.0 and y>=0.0)]
            ----------------------------------------
            [z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
            [z1z3]
            [z3,(l3),(x>=0.0 and y>=0.0)]
            ----------------------------------------
            [z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
            [z1z4]
            [z4,(l0),(x>2.0 and y>2.0)]
            ----------------------------------------
            [z2,(l2),(0.0<=x<1.0 and y>=0.0)]
            [z2z3]
            [z3,(l3),(x>=0.0 and y>=0.0)]
        """.trimIndent()
        )

        assert(
            rs2.trim() == """
            [z0][z0z1][1.0]
            [z1][z1z2][1.0]
            [z1][z1z3][1.0]
            [z1][z1z4][1.0]
            [z2][z2z3][1.0]
        """.trimIndent()
        )
        assert(
            rs3.trim() == """
            ZonePath((z0,z0z1,z1),(z1,z1z2,z2),(z2,z2z3,z3),pReward=20.0)
            ZonePath((z0,z0z1,z1),(z1,z1z3,z3),pReward=20.0)
            ZonePath((z0,z0z1,z1),(z1,z1z4,z4),pReward=16.666666666666664)
        """.trimIndent()
        )
    }

    @Test
    fun t3t2() {
        val zoneGraph = getZoneGraphObj3().also {
            val zone2 = it.stateLHS.toArray()[1]!!
            it.stateEventPLHM.let {
                //概率生效
                it[zone2]!!["z1z3"] = 2.0
                it[zone2]!!["z1z4"] = 2.0
            }
        }
        val rs1 = zoneGraph.stateEventTLHM.touchPrintlnType2()
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
            ----------------------------------------
            [z0,(l0),(x-y=0.0)]
            [z0z1]
            [z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
            ----------------------------------------
            [z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
            [z1z2]
            [z2,(l2),(0.0<=x<1.0 and y>=0.0)]
            ----------------------------------------
            [z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
            [z1z3]
            [z3,(l3),(x>=0.0 and y>=0.0)]
            ----------------------------------------
            [z1,(l1),(0.0<=x<1.0 and x-y<=0.0)]
            [z1z4]
            [z4,(l0),(x>2.0 and y>2.0)]
            ----------------------------------------
            [z2,(l2),(0.0<=x<1.0 and y>=0.0)]
            [z2z3]
            [z3,(l3),(x>=0.0 and y>=0.0)]
        """.trimIndent()
        )
        assert(
            rs2.trim() == """
            [z0][z0z1][1.0]
            [z1][z1z2][1.0]
            [z1][z1z3][2.0]
            [z1][z1z4][2.0]
            [z2][z2z3][1.0]
        """.trimIndent()
        )
        assert(
            rs3.trim() == """
            ZonePath((z0,z0z1,z1),(z1,z1z2,z2),(z2,z2z3,z3),pReward=12.0)
            ZonePath((z0,z0z1,z1),(z1,z1z3,z3),pReward=24.0)
            ZonePath((z0,z0z1,z1),(z1,z1z4,z4),pReward=20.0)
        """.trimIndent()
        )
    }

    @Test
    fun t4t1() {
        val zoneGraph = getZoneGraphObj3()
        val sb1 = StringBuilder()
        zoneGraph.stateLHS.map {
            sb1.append("${it.toString2()}\n")
            sb1.append("${zoneGraph.stateLHS.indexOf(it)}\n")
        }
        val rs1 = sb1.toString()
        println(rs1)
        assert(
            rs1.trim() == """
            z0,(l0),(x-y=0.0)
            0
            z1,(l1),(0.0<=x<1.0 and x-y<=0.0)
            1
            z2,(l2),(0.0<=x<1.0 and y>=0.0)
            2
            z3,(l3),(x>=0.0 and y>=0.0)
            3
            z4,(l0),(x>2.0 and y>2.0)
            4
        """.trimIndent()
        )
    }
}