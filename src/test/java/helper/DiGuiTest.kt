package helper

import org.junit.Test

internal class DiGuiTest {
    fun calculate(
        lhm: LinkedHashMap<Int, String>,
        index: Int,
        ifFirstIn: Boolean = true,
    ) {
        if (ifFirstIn) {
            (0..2).map {
                calculate(lhm, it, false)
                println(lhm)
            }
        } else {
            lhm[index] = "${lhm[index]}hh${index}"
        }
    }

    @Test
    fun t1() {
        val lhm = linkedMapOf(1 to "s1", 2 to "s2", 3 to "s3")
        calculate(lhm, 0)
    }
}