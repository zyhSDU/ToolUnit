package helper

import helper.base.PrintHelper.StringTo.toPrintln
import org.junit.Test

internal class ClassLifecycleTest {
    @Test
    fun t1() {
        class Home(
            val father: String="f1",
            val mather: String="m1",
        ) {
            val child1: String = "11"
            val child2: String = "2".let {
                val res = "${it}${it}"
                res.toPrintln()
                res
            }
            val child3: String

            init {
                "init".toPrintln()
                child3 = "33"
                child3.toPrintln()
            }

            override fun toString(): String {
                return "Home(father='$father', mather='$mather', child1='$child1', child2='$child2', child3='$child3')"
            }
        }

        val home = Home()
        home.toString().toPrintln()
    }
}