package helper.scxml.scxml1

import helper.base.PrintHelper.StringTo.toPrintln
import helper.scxml.scxml1.Scxml1Helper.GetTestObject.getTestState
import helper.scxml.scxml1.Scxml1Helper.GetTestObject.getTestStatus
import helper.scxml.scxml1.Scxml1Helper.GetTestObject.getTestTransition
import helper.scxml.scxml1.Scxml1Helper.ToSelfBlock.toSelfBlock
import helper.scxml.strategy.ScxmlOneStrategyHelper.SNode
import helper.scxml.ScxmlVarHelper.IntConstraint.ToIntConstraint.toIntConstraint
import org.junit.Test

internal class OtherTest {
    @Test
    fun testTransitionToSelfBlockTest() {
        val transition = getTestTransition()
        transition.toSelfBlock().printCode()
    }

    @Test
    fun testStateGetInfoTest() {
        val state = getTestState()
        state.toSelfBlock().printCode()
    }

    @Test
    fun testStatusGetInfoTest() {
        val status = getTestStatus()
        status.toSelfBlock().printCode()
    }

    @Test
    fun testToIntConstraint() {
        "0 <= T <= 2".toIntConstraint().toString().toPrintln()
    }

    @Test
    fun testStrategyNodeNodeTypeTest() {
        val envStrategyNode = SNode.getRootNode().also {
            it.addMiddleNode0("Aalborg", "bike" to 1.0)
            it.addMiddleNode0("Bike", "bike_end" to 1.0)
        }
        envStrategyNode.toBlock().getStr().toPrintln()
    }

    class Location(
        val ids: ArrayList<Int>,
    )

    class LocationUnit(
        val location: Location,
        val children: ArrayList<LocationUnit> = ArrayList(),
    )
}
