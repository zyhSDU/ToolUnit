package helper

import helper.base.PrintHelper.StringTo.toPrintln
import org.junit.Test

internal class StringHelperTest {
    @Test
    fun t1() {
        val str1 = "Hello World"  // 双引号创建字符串
        val str2 = """Hello
             World"""    // 三引号创建多行字符串
        str1.toPrintln()
        str2.toPrintln()
    }

    @Test
    fun t2() {
        val str = "Hello World"
        val contains1 = str.contains("Hello")  // 查找是否包含"Hello"子串
        val contains2 = str.contains("world", ignoreCase = true)  // 忽略大小写查找是否包含"world"子串
        contains1.toString().toPrintln()
        contains2.toString().toPrintln()
    }

    @Test
    fun t3(){
        val str = "Hello World"
        val newStr = str.replace("Hello", "Hi")  // 将"Hello"替换为"Hi"
        newStr.toPrintln()
    }

    @Test
    fun t4(){
        val arrayListOf = arrayListOf("a1", "a2", "a3")
        arrayListOf.joinToString {
            it
        }
    }
    object TestRun{
        @JvmStatic
        fun main(args: Array<String>) {

        }
    }
}