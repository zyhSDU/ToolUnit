package helper

import org.junit.Test

internal class GrammarHelperTest {
    @Test
    fun grammarTest1() {
        val s1 = "s1"
        var s2 = s1
        s2 = s2.replace("1", "2")
        println(s1)
        println(s2)
    }

    @Test
    fun grammarTest2SubstringAfter() {
        val s1 = "abcd123abc123"
        val s2 = s1.substringAfter("d")
        println(s2)
    }

    @Test
    fun grammarTest3Arraylist() {
        fun t3f1(
            replace_list: ArrayList<String> = ArrayList(),
        ): ArrayList<String> {
            return replace_list
        }

        val tf1 = t3f1()
        tf1.add("1")
        val tf2 = t3f1()
        println(tf1)
        println(tf2)
    }
}