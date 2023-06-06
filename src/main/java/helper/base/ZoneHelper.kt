package helper.base

object ZoneHelper {
    open class ZoneTransition<E>(
        open val start: E,
        open val event: String,
        open val end: E,
    )

    open class StateEventTransitionLHM<E, F> : LHMHelper.A3LHM<E, String, F>() {
        override fun touch(
            init: (E, String, F) -> Unit,
        ) {
            this.map { (a1, a23LHM) ->
                a23LHM.map { (a2, a3) ->
                    init(a1, a2, a3)
                }
            }
        }
    }

    open class StateEventPLHM<E> : LHMHelper.A3LHM<E, String, Double>() {
        override fun touch(
            init: (E, String, Double) -> Unit,
        ) {
            this.map { (a1, a23LHM) ->
                a23LHM.map { (a2, a3) ->
                    init(a1, a2, a3)
                }
            }
        }
    }

    open class ZonePath<E>(
        var pReward: Double = 1.0,
    ) : ArrayList<E>() {
        fun add(zoneTransition: E, pReward: Double = 1.0) {
            this.add(zoneTransition)
            this.pReward *= pReward
        }
    }
}