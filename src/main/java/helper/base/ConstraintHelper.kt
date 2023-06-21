package helper.base

import helper.base.ConstraintHelper.CompositeConstraint.Expand.toCompositeConstraint
import helper.base.ConstraintHelper.DiffHelper.Expand.toDiffDouble
import helper.base.ConstraintHelper.DiffHelper.Expand.toDiffString
import helper.base.ConstraintHelper.JoinOp.Companion.andOp

object ConstraintHelper {
    object DiffHelper {
        object Expand {
            fun ArrayList<String>.toDiffString(): String {
                return this.joinToString(separator = "-")
            }

            fun ArrayList<String>.toDoubleList(
                vars: LinkedHashMap<String, Double>,
            ): ArrayList<Double> {
                val convertedList = ArrayList<Double>()

                for (item in this) {
                    if (!vars.containsKey(item)) throw IllegalArgumentException()
                    convertedList.add(vars[item]!!)
                }

                return convertedList
            }

            fun ArrayList<Double>.toDiffDouble(): Double {
                if (this.size == 0) throw IllegalArgumentException()

                var totalD = 0.0

                for ((i, d) in this.withIndex()) {
                    if (i == 0) {
                        totalD += d
                    } else {
                        totalD -= d
                    }
                }
                return totalD
            }

            fun ArrayList<String>.toDiffDouble(
                vars: LinkedHashMap<String, Double>,
            ): Double {
                return this.toDoubleList(vars).toDiffDouble()
            }
        }
    }

    class CompareOp(
        val name: String,
        val meetFun: (Double, Double) -> Boolean,
    ) {
        override fun toString(): String {
            return name
        }

        companion object {
            val eOp = CompareOp("=") { v1, v2 -> v1 == v2 }
            val lOp = CompareOp("<") { v1, v2 -> v1 < v2 }
            val gOp = CompareOp(">") { v1, v2 -> v1 > v2 }
            val leOp = CompareOp("<=") { v1, v2 -> v1 <= v2 }
            val geOp = CompareOp(">=") { v1, v2 -> v1 >= v2 }

            private val oppositeCompareOperatorLHM = LinkedHashMap<CompareOp, CompareOp>().also {
                it[eOp] = eOp
                it[lOp] = gOp
                it[gOp] = lOp
                it[leOp] = geOp
                it[geOp] = leOp
            }

            fun getOppositeCompareOpName(compareOp: CompareOp): String {
                return oppositeCompareOperatorLHM[compareOp]!!.name
            }
        }
    }

    class JoinOp(
        val name: String,
        val meetFun: (
            CompositeConstraint,
            CompositeConstraint,
            LinkedHashMap<String, Double>,
        ) -> Boolean,
    ) {
        companion object {
            val andOp = JoinOp("and") { c1, c2, vars ->
                c1.meet(vars) && c2.meet(vars)
            }
            val orOp = JoinOp("or") { c1, c2, vars ->
                c1.meet(vars) || c2.meet(vars)
            }
        }
    }

    //0or1
    interface N0N1Constraint {
        fun meet(
            vars: LinkedHashMap<String, Double>?
        ): Boolean
    }

    //1or2
    //其中var相同
    interface N1N2Constraint {
        fun getDiffString(): String
    }

    class N0Constraint(
        val boolean: Boolean,
    ) : N0N1Constraint {
        fun meet(
        ): Boolean {
            return boolean
        }

        override fun meet(
            vars: LinkedHashMap<String, Double>?
        ): Boolean {
            return meet()
        }
    }

    class N1Constraint(
        val vs: ArrayList<String>,
        val cp: CompareOp,
        val d: Double,
    ) : N0N1Constraint, N1N2Constraint {
        override fun meet(
            vars: LinkedHashMap<String, Double>?
        ): Boolean {
            if (vars == null) throw IllegalArgumentException()
            return cp.meetFun(
                vs.toDiffDouble(vars),
                d,
            )
        }

        override fun getDiffString(): String {
            return vs.toDiffString()
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(vs.toDiffString())
            sb.append(this.cp)
            sb.append(this.d)
            return sb.toString()
        }

        companion object {
            fun getV1N1Constraint(
                v1: String,
                c: CompareOp,
                d: Double,
            ): N1Constraint {
                return N1Constraint(
                    arrayListOf(v1),
                    c,
                    d,
                )
            }

            fun getV2N1Constraint(
                v1: String,
                v2: String,
                c: CompareOp,
                d: Double,
            ): N1Constraint {
                return N1Constraint(
                    arrayListOf(v1, v2),
                    c,
                    d,
                )
            }

            fun getXN1Constraint(
                c: CompareOp,
                d: Double,
            ): N1Constraint {
                return getV1N1Constraint(
                    "x",
                    c,
                    d,
                )
            }

            fun getYN1Constraint(
                c: CompareOp,
                d: Double,
            ): N1Constraint {
                return getV1N1Constraint(
                    "y",
                    c,
                    d,
                )
            }

            fun getXYN1Constraint(
                c: CompareOp,
                d: Double,
            ): N1Constraint {
                return getV2N1Constraint(
                    "x",
                    "y",
                    c,
                    d,
                )
            }
        }
    }

    //0,1,2,...
    open class CompositeConstraint(
        val cc0: N0N1Constraint? = null,
        val cc1: CompositeConstraint? = null,
        val op: JoinOp? = null,
        val cc2: CompositeConstraint? = null,
    ) {
        fun meet(
            vars: LinkedHashMap<String, Double>?,
        ): Boolean {
            return if (cc0 != null) {
                cc0.meet(vars)
            } else {
                if (cc1 == null) throw IllegalArgumentException()
                if (op == null) {
                    cc1.meet(vars)
                } else {
                    if (cc2 == null) throw IllegalArgumentException()
                    op.meetFun(cc1, cc2, vars!!)
                }
            }
        }

        override fun toString(): String {
            val sb = StringBuilder()
            if (cc0 != null) {
                sb.append(cc0.toString())
            } else {
                if (cc1 != null) {
                    sb.append("(${cc1})")
                    if (op == null) throw IllegalArgumentException()
                    sb.append(" ${op.name} ")
                }
                sb.append(cc2.toString())
            }
            return sb.toString()
        }

        companion object {
            fun getCompositeConstraint(
                cc1: CompositeConstraint? = null,
                op: JoinOp? = null,
                cc2: CompositeConstraint? = null,
            ): CompositeConstraint {
                return CompositeConstraint(
                    null,
                    cc1,
                    op,
                    cc2,
                )
            }
        }

        object Expand {
            fun N0Constraint.toCompositeConstraint(): CompositeConstraint {
                return CompositeConstraint(this)
            }

            fun N1Constraint.toCompositeConstraint(): CompositeConstraint {
                return CompositeConstraint(this)
            }
        }
    }

    //这只是一种类型
    //其中var相同
    class N2Constraint(
        val vs: ArrayList<String>,
        val c1: CompareOp,
        val d1: Double,
        val c2: CompareOp,
        val d2: Double,
    ) : CompositeConstraint(
        null,
        N1Constraint(vs, c1, d1).toCompositeConstraint(),
        andOp,
        N1Constraint(vs, c2, d2).toCompositeConstraint(),
    ), N1N2Constraint {
        override fun getDiffString(): String {
            return vs.toDiffString()
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(d1)
            sb.append(CompareOp.getOppositeCompareOpName(c1))
            sb.append(vs.toDiffString())
            sb.append(c2)
            sb.append(d2)
            return sb.toString()
        }

        companion object {
            fun getV1N2Constraint(
                v1: String,
                c1: CompareOp,
                d1: Double,
                c2: CompareOp,
                d2: Double,
            ): N2Constraint {
                return N2Constraint(
                    arrayListOf(v1),
                    c1,
                    d1,
                    c2,
                    d2,
                )
            }

            fun getV2N2Constraint(
                v1: String,
                v2: String,
                c1: CompareOp,
                d1: Double,
                c2: CompareOp,
                d2: Double,
            ): N2Constraint {
                return N2Constraint(
                    arrayListOf(v1, v2),
                    c1,
                    d1,
                    c2,
                    d2,
                )
            }

            fun getXN2Constraint(
                c1: CompareOp,
                d1: Double,
                c2: CompareOp,
                d2: Double,
            ): N2Constraint {
                return getV1N2Constraint(
                    "x",
                    c1,
                    d1,
                    c2,
                    d2,
                )
            }
        }
    }

    object N1N2ConstraintHelper {
        fun getV1Constraint(
            v1: String,
            c: CompareOp,
            d: Double,
        ): N1Constraint {
            return N1Constraint.getV1N1Constraint(v1, c, d)
        }

        fun getV1Constraint(
            v1: String,
            c1: CompareOp,
            d1: Double,
            c2: CompareOp,
            d2: Double,
        ): N2Constraint {
            return N2Constraint.getV1N2Constraint(v1, c1, d1, c2, d2)
        }

        fun getV2Constraint(
            v1: String,
            v2: String,
            c: CompareOp,
            d: Double,
        ): N1Constraint {
            return N1Constraint.getV2N1Constraint(v1, v2, c, d)
        }

        fun getV2Constraint(
            v1: String,
            v2: String,
            c1: CompareOp,
            d1: Double,
            c2: CompareOp,
            d2: Double,
        ): N2Constraint {
            return N2Constraint.getV2N2Constraint(v1, v2, c1, d1, c2, d2)
        }

        fun getXConstraint(
            c: CompareOp,
            d: Double,
        ): N1Constraint {
            return getV1Constraint("x", c, d)
        }

        fun getXConstraint(
            c1: CompareOp,
            d1: Double,
            c2: CompareOp,
            d2: Double,
        ): N2Constraint {
            return getV1Constraint("x", c1, d1, c2, d2)
        }

        fun getYConstraint(
            c: CompareOp,
            d: Double,
        ): N1Constraint {
            return getV1Constraint("y", c, d)
        }

        fun getYConstraint(
            c1: CompareOp,
            d1: Double,
            c2: CompareOp,
            d2: Double,
        ): N2Constraint {
            return getV1Constraint("y", c1, d1, c2, d2)
        }

        fun getXYConstraint(
            c: CompareOp,
            d: Double,
        ): N1Constraint {
            return getV2Constraint("x", "y", c, d)
        }

        fun getXYConstraint(
            c1: CompareOp,
            d1: Double,
            c2: CompareOp,
            d2: Double,
        ): N2Constraint {
            return getV2Constraint("x", "y", c1, d1, c2, d2)
        }
    }
}