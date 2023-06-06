package helper.scxml.scxml2

import helper.base.ConstraintHelper.N1N2Constraint
import helper.base.ZoneHelper
import helper.scxml.scxml2.ZoneHelper.StateEventPLHM.Expand.toP1StateEventPLHM

object ZoneHelper {
    class ZoneState(
        val locations: ArrayList<String>,
        val cons: ArrayList<N1N2Constraint>,
        var reward: Double = 0.0,
        var name: String = "",
    ) {
        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("(${locations.joinToString(",")})")
            sb.append(",")
            sb.append(cons.joinToString(","))
            return sb.toString()
        }

        fun toString2(): String {
            val sb = StringBuilder()
            sb.append("${name},")
            sb.append("(${locations.joinToString(",")})")
            sb.append(",")
            sb.append("(${cons.joinToString(" and ")})")
            return sb.toString()
        }

        companion object {
            fun getL1ZoneState(
                location: String,
                cons: ArrayList<N1N2Constraint>,
                reward: Double = 0.0,
            ): ZoneState {
                return ZoneState(
                    arrayListOf(location),
                    cons,
                    reward,
                )
            }

            fun getL1C1ZoneState(
                location: String,
                c1: N1N2Constraint,
                reward: Double = 0.0,
            ): ZoneState {
                return getL1ZoneState(
                    location,
                    arrayListOf(c1),
                    reward,
                )
            }

            fun getL1C2ZoneState(
                location: String,
                c1: N1N2Constraint,
                c2: N1N2Constraint,
                reward: Double = 0.0,
            ): ZoneState {
                return getL1ZoneState(
                    location,
                    arrayListOf(c1, c2),
                    reward,
                )
            }
        }
    }

    data class ZoneTransition(
        override val start: ZoneState,
        override var event: String,
        override val end: ZoneState,
    ) : ZoneHelper.ZoneTransition<ZoneState>(
        start,
        event,
        end,
    ) {
        fun clone(): ZoneTransition {
            return this.copy()
        }

        companion object {
            fun getEmptyEventZoneTransition(
                start: ZoneState,
                end: ZoneState,
            ): ZoneTransition {
                return ZoneTransition(start, "", end)
            }
        }
    }

    class StateEventTransitionLHM : ZoneHelper.StateEventTransitionLHM<ZoneState, ZoneTransition>() {
        fun touchPrintlnType1(
            sb: StringBuilder = StringBuilder(),
        ): String {
            this.touch { a1, a2, a3 ->
                sb.append("${a1},${a2},${a3.end}\n")
            }
            return sb.toString()
        }

        fun touchPrintlnType2(
            sb: StringBuilder = StringBuilder(),
        ): String {
            this.touch { a1, a2, a3 ->
                sb.append("-".repeat(40))
                sb.append("\n")
                sb.append("[${a1.toString2()}]\n")
                sb.append("[${a2}]\n")
                sb.append("[${a3.end.toString2()}]\n")
            }
            return sb.toString()
        }
    }

    class StateEventPLHM : ZoneHelper.StateEventPLHM<ZoneState>() {
        fun touchPrintlnType1(
            sb: StringBuilder = StringBuilder(),
        ): StringBuilder {
            this.touch { a1, a2, a3 ->
                sb.append("[${a1.name}][${a2}][${a3}]\n")
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
            stateLHS.map {
                it.name = "z${stateLHS.indexOf(it)}"
            }
            ts.map {
                it.event = "${it.start.name}${it.end.name}"
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
                sb.append("[${start.toString2()}]\n")
            } else {
                nowZT!!.let {
                    sb.append("${"\t".repeat(tabSize)}[${it.event}][${it.end.toString2()}]\n")
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
            val isNowZTNull = nowZT == null
            if (!isNowZTNull) {
                nowZT!!
                val totalP = this.stateEventPLHM[nowZT.start]!!.values.sum()
                val nowP = this.stateEventPLHM[nowZT.start]!![nowZT.event]!!
                nowPath.add(nowZT, nowP / totalP)
            }
            val nowState: ZoneState = if (isNowZTNull) {
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
            if (!isNowZTNull) {
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
}