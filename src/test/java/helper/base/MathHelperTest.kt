package helper.base

import helper.scxml.scxml2.MathHelper.Expand.mathMinus
import org.junit.Test

internal class MathHelperTest {
    @Test
    fun t1() {
        val a1 = arrayListOf(1.0,2.0,4.0)
        val a2 = arrayListOf(2.0,4.0,8.0)
        val a3= a1.mathMinus(a2)
        println(a3)
    }

    @Test
    fun t2() {
        repeat(10000) {
            linkedMapOf(
                "s1s2" to 1.0,
                "s1s3" to 1.0,
                "s1s4t1" to 1.0,
            ).let {
                val randomString = MathHelper.getRandomString(it)
                if (randomString == null) {
                    println("randomString is null")
                }
            }
        }
    }
}