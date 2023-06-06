package helper.base

import java.io.Serializable

object TupleHelper {
    data class FourTuple<out A, out B, out C, out D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
    ) : Serializable {

        override fun toString(): String = "($first, $second, $third, $fourth,)"
    }

    fun <T> FourTuple<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)

    data class FiveTuple<out A, out B, out C, out D, out E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E,
    ) : Serializable


    data class SixTuple<out A, out B, out C, out D, out E, out F>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E,
        val sixth: F,
    ) : Serializable
}
