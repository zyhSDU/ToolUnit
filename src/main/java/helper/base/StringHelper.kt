package helper.base

import helper.base.StringHelper.StringTo.to010String
import helper.base.StringHelper.StringTo.to01String

object StringHelper {
    fun get01String(
        s0: String,
        s1: String,
    ): String = "${s0}${s1}"

    fun get010String(
        s0: String,
        s1: String,
    ): String = "${s0}${s1}${s0}"

    object StringTo {
        fun String.to01String(
            s1: String,
        ): String = get01String(this, s1)

        fun String.to010String(
            s1: String,
        ): String = get010String(this, s1)

        fun String.toMathFormulaString() = "\$".to010String(this)
    }

    fun titleString(
        string: String,
        number: Int,
        title: String,
    ): String = string.repeat(number).to010String(title)

    fun tabsString(
        tabNumber: Int,
        string: String,
    ): String = "\t".repeat(tabNumber).to01String(string)

    // 去除字符串中的空行
    // 使用正则表达式 [\r\n]+ 匹配字符串中的一个或多个回车符或换行符。
    // 注意，这里使用了字符类 [...] 来匹配多个字符，因为回车符和换行符可以在不同的操作系统中以不同的方式表示。
    fun String.removeEmptyLine(): String = this.replace(Regex("[\r\n]+"), "\n")

    fun String.replaceMany(
        kv: LinkedHashMap<String, String>,
    ): String {
        var returnString = this
        kv.map { (k, v) ->
            returnString = returnString.replace(k, v)
        }
        return returnString
    }

    fun String.removeEmpty(): String = this.replace(Regex("\\s+"), "")
}