package helper.base

import java.util.regex.Matcher
import java.util.regex.Pattern

object RegexHelper {
    //待改，下面两行就可以了
    //    val regex = Regex("(\\w+)\\s*==\\s*(\\d+)")
    //    val matchResult = regex.find(input)
    fun oldMatch(pattern: String, matcherString: String): Pair<Boolean, Matcher> {
        val compile = Pattern.compile(pattern)
        val matcher = compile.matcher(matcherString)
        return Pair(matcher.find(), matcher)
    }

    //弃？
    enum class PattenEnum(val pattern: String, val description: String) {
        p1("^[0-9]*$", "数字"),
        ;
    }

    fun match(
        regex: String,
        text: String,
    ): Matcher {
        val pattern = Pattern.compile(regex)
        return pattern.matcher(text)
    }

    //<state id="switch_up">
    fun matchStateId(inputString: String): String? {
        val matcher = match("<state id=\"(\\w+)\">", inputString)

        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    fun String.matchPrefix(prefix: String): String? {
        match("${prefix}_(\\w+)", this).let {
            if (it.find()) {
                return it.group(1)
            }
        }
        return null
    }

    fun String.match(regex: Regex): MatchResult? {
        return regex.find(this)
    }
}