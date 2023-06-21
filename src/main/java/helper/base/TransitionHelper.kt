package helper.base

import helper.base.LHMHelper.A3LHM

object TransitionHelper {
    open class Transition<E>(
        open val start: E,
        open val event: String,
        open val end: E,
    )

    open class EventTransitionLHM<E, F> : A3LHM<E, String, F>() {
//        override fun touch(
//            init: (E, String, F) -> Unit,
//        ) {
//            this.map { (a1, a2LHM) ->
//                a2LHM.map { (a2, a3) ->
//                    init(a1, a2, a3)
//                }
//            }
//        }
    }

    open class EventPLHM<E> : A3LHM<E, String, Double>() {
//        override fun touch(
//            init: (E, String, Double) -> Unit,
//        ) {
//            this.map { (a1, a2LHM) ->
//                a2LHM.map { (a2, a3) ->
//                    init(a1, a2, a3)
//                }
//            }
//        }
    }

    open class Path<E>(
        var pReward: Double = 1.0,
    ) : ArrayList<E>() {
        fun add(
            transition: E,
            pReward: Double = 1.0,
        ) {
            this.add(transition)
            this.pReward *= pReward
        }
    }
}