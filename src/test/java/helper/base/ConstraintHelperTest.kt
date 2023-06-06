package helper.base

import helper.base.ConstraintHelper.CompareOperator.Companion.eOp
import helper.base.ConstraintHelper.CompareOperator.Companion.geOp
import helper.base.ConstraintHelper.CompareOperator.Companion.lOp
import helper.base.ConstraintHelper.CompareOperator.Companion.leOp
import helper.base.ConstraintHelper.CompositeConstraint
import helper.base.ConstraintHelper.CompositeConstraint.Companion.getCompositeConstraint
import helper.base.ConstraintHelper.CompositeConstraint.Expand.toCompositeConstraint
import helper.base.ConstraintHelper.JoinOperator.Companion.andOp
import helper.base.ConstraintHelper.JoinOperator.Companion.orOp
import helper.base.ConstraintHelper.N1Constraint
import helper.base.ConstraintHelper.N2Constraint
import helper.base.ConstraintHelperTest.OneOrTwoConstraintHelper.getOneConstraintObj1
import helper.base.ConstraintHelperTest.OneOrTwoConstraintHelper.getTwoConstraintObj1
import helper.base.ConstraintHelperTest.OneOrTwoConstraintHelper.getTwoConstraintObj2
import org.junit.Test

internal class ConstraintHelperTest {
    object OneOrTwoConstraintHelper {
        fun getTwoConstraintObj1(): N2Constraint {
            return N2Constraint(
                arrayListOf("x", "y"),
                geOp,
                0.0,
                lOp,
                1.0
            )
        }

        fun getTwoConstraintObj2(): N2Constraint {
            return N2Constraint(
                arrayListOf("x"),
                geOp,
                0.0,
                lOp,
                1.0
            )
        }

        fun getOneConstraintObj1(): CompositeConstraint {
            return N1Constraint(
                arrayListOf("x", "y"),
                leOp,
                0.0,
            ).toCompositeConstraint()
        }
    }

    @Test
    fun t1t1() {
        val constraint = N1Constraint(
            arrayListOf("x", "y"),
            eOp,
            0.0,
        )
        val rs = constraint.toString()
        println(rs)
        assert(
            rs == """
            x-y=0.0
        """.trimIndent()
        )

        val lhm = LinkedHashMap<String, Double>()
        lhm["x"] = 1.0
        lhm["y"] = 1.0
        val meet1 = constraint.meet(lhm)
        println(meet1)
        assert(meet1)
        lhm["y"] = 2.0
        val meet2 = constraint.meet(lhm)
        println(meet2)
        assert(!meet2)
    }

    @Test
    fun t2t1() {
        val twoConstraint = getCompositeConstraint(
            N1Constraint(
                arrayListOf("x", "y"),
                geOp,
                0.0,
            ).toCompositeConstraint(),
            andOp,
            N1Constraint(
                arrayListOf("x", "y"),
                lOp,
                1.0,
            ).toCompositeConstraint(),
        )
        val rs = twoConstraint.toString()
        println(rs)
        assert(
            rs == """
            (x-y>=0.0) and x-y<1.0
        """.trimIndent()
        )
    }

    @Test
    fun t2t2() {
        val constraint = getTwoConstraintObj1()
        val rs = constraint.toString()
        println(rs)
        assert(
            rs == """
            0.0<=x-y<1.0
        """.trimIndent()
        )
        val lhm = LinkedHashMap<String, Double>()
        lhm["x"] = 1.0
        lhm["y"] = 1.0
        var meet = constraint.meet(lhm)
        println(meet)
        assert(meet)
        lhm["x"] = 2.0
        meet = constraint.meet(lhm)
        println(meet)
        assert(!meet)
    }

    @Test
    fun t3t1() {
        val twoConstraint = getCompositeConstraint(
            getCompositeConstraint(
                N1Constraint(
                    arrayListOf("x"),
                    geOp,
                    0.0,
                ).toCompositeConstraint(),
                andOp,
                N1Constraint(
                    arrayListOf("y"),
                    lOp,
                    1.0,
                ).toCompositeConstraint()
            ),
            orOp,
            N1Constraint(
                arrayListOf("z"),
                eOp,
                2.0,
            ).toCompositeConstraint()
        )
        val rs = twoConstraint.toString()
        println(rs)
        assert(
            rs == """
            ((x>=0.0) and y<1.0) or z=2.0
        """.trimIndent()
        )
    }

    @Test
    fun t3t2() {
        val cc = getCompositeConstraint(
            getTwoConstraintObj2(),
            andOp,
            getOneConstraintObj1(),
        )
        val rs = cc.toString()
        println(cc)
        assert(
            rs == """
            (0.0<=x<1.0) and x-y<=0.0
        """.trimIndent()
        )
    }
}