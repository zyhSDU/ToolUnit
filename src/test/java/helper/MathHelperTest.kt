package helper

import helper.base.MathHelper
import helper.base.PrintHelper.StringTo.toPrintln
import org.junit.Test
import kotlin.random.Random

internal class MathHelperTest {
    @Test
    fun t1() {
        repeat(100) {
            Random.nextDouble().toString().toPrintln()
        }
    }

    @Test
    fun t2() {
        LinkedHashMap<String, Double>().also {
            it["k1"] = 1.0
            it["k2"] = 1.0
        }.also {
            var countK1 = 0
            var countK2 = 0
            var countNull = 0
            repeat(40000000) { repeatIndex ->
                MathHelper.getRandomStringWithLeftTime(it, 2).let {
                    if (it == null) {
                        countNull += 1
                    } else {
                        if (it == "k1") {
                            countK1 += 1
                        }
                        if (it == "k2") {
                            countK2 += 1
                        }
                    }
                }
            }
            "${countNull},${countK1},${countK2}".toPrintln()
        }
    }
}