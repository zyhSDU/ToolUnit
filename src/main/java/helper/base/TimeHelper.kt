package helper.base

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TimeHelper {
    fun now(pattern: String): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern))
    }

    enum class TimePattern(val pattern: String) {
        p1("@yyyy-MMdd-HHmmss"),
        p2("@yyyy-MMdd"),
        p3("@yyyy-MMdd-HHmm-ss-SSS"),
        p4("yyyy_MMdd_HHmm_ss_SSS"),
        ;
    }

    fun now(timePattern: TimePattern = TimePattern.p2): String {
        return now(timePattern.pattern)
    }

    var count = 0
    var countMax = 1000
    fun nowCount1000(): String {
        return "${now(TimePattern.p4)}_${String.format("%03d", (count++) % countMax)}"
    }
}