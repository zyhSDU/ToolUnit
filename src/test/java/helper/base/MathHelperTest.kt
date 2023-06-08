package helper.base

import org.junit.Test

internal class MathHelperTest {
    @Test
    fun t1() {
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