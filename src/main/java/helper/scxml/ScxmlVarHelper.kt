package helper.scxml

import helper.base.PrintHelper.StringTo.toPrintln
import helper.scxml.ScxmlVarHelper.IntConstraint.ToIntConstraint.toIntConstraint
import org.apache.commons.scxml.model.Data

object ScxmlVarHelper {
    class ComparisonSymbol(
        val name: String,
    ) {
        companion object {
            val lt = ComparisonSymbol("<")
            val le = ComparisonSymbol("<=")
        }

        override fun toString(): String {
            return name
        }
    }

    interface VarConstraint {
        var minComparisonSymbol: ComparisonSymbol
        var varId: String
        var maxComparisonSymbol: ComparisonSymbol
    }

    open class DoubleConstraint(
        var minV: Double = 0.0,
        override var minComparisonSymbol: ComparisonSymbol = ComparisonSymbol.le,
        override var varId: String = "id",
        override var maxComparisonSymbol: ComparisonSymbol = ComparisonSymbol.le,
        var maxV: Double = 0.0,
    ) : VarConstraint {
        override fun toString(): String {
            return "" +
                    "$minV " +
                    "$minComparisonSymbol " +
                    "$varId " +
                    "$maxComparisonSymbol " +
                    "$maxV"
        }
    }

    open class IntConstraint(
        override var varId: String = "id",
        val intRange: IntRange = 0..0,
    ) : VarConstraint {
        override var minComparisonSymbol: ComparisonSymbol = ComparisonSymbol.le
        override var maxComparisonSymbol: ComparisonSymbol = ComparisonSymbol.le
        val minV = intRange.first
        val maxV = intRange.last
        override fun toString(): String {
            return "" +
                    "$minV " +
                    "$minComparisonSymbol " +
                    "$varId " +
                    "$maxComparisonSymbol " +
                    "$maxV"
        }

        object ToIntConstraint {
            fun String.toIntConstraint(): IntConstraint {
                var regex: Regex
                var matchResult: MatchResult?

                regex = Regex("(\\w+)\\s*==\\s*(\\d+)")
                matchResult = regex.find(this)
                if (matchResult != null) {
                    val a1 = matchResult.groupValues[1]
                    val a2 = matchResult.groupValues[2].toInt()
                    return IntConstraint(
                        varId = a1,
                        intRange = a2..a2
                    )
                }

                regex = Regex("(\\d+)\\s*\\S+\\s*(\\w+)\\s+and\\s+(\\w+)\\s*\\S+\\s*(\\d+)")
                matchResult = regex.find(this)
                if (matchResult != null) {
                    val a1 = matchResult.groupValues[2]
                    val a2 = matchResult.groupValues[1].toInt()
                    val a3 = matchResult.groupValues[4].toInt()
                    return IntConstraint(
                        varId = a1,
                        intRange = a2..a3
                    )
                }
                val sp = this.split(" ")
                assert(sp.size == 5)
                return IntConstraint(
                    varId = sp[2],
                    intRange = sp[0].toInt()..sp[4].toInt(),
                )
            }

            @JvmStatic
            fun main(args: Array<String>) {
                "42 <= T and T <= 45".toIntConstraint().toString().toPrintln()
            }
        }
    }

    class ClockConstraint(
        varId: String = "id",
        intRange: IntRange = 0..0,
    ) : IntConstraint(
        varId,
        intRange,
    ) {
        constructor(
            varId: String,
            intRange: Int,
        ) : this(
            varId,
            intRange..intRange,
        )

        fun ifMeet(
            data: Int,
        ): Boolean {
            return data in intRange
        }

        fun ifMeet(
            data: String,
        ): Boolean {
            return this.ifMeet(data.toInt())
        }

        fun ifMeet(
            data: Data,
        ): Boolean {
            return ifMeet(data.expr)
        }

        fun ifMeet(
            data: org.apache.commons.scxml2.model.Data,
        ): Boolean {
            return ifMeet(data.expr)
        }

        fun ifMeet(
            lhm: LinkedHashMap<String, org.apache.commons.scxml2.model.Data>,
        ): Boolean {
            return ifMeet(lhm[varId]!!)
        }

        object ToClockConstraint {
            fun IntConstraint.toClockConstraint(): ClockConstraint {
                return ClockConstraint(
                    varId,
                    intRange,
                )
            }

            fun String?.toClockConstraint(): ClockConstraint? {
                if (isNullOrEmpty()) return null
                return this.toIntConstraint().toClockConstraint()
            }
        }
    }
}