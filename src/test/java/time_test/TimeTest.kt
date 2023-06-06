package time_test

import helper.base.TimeHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TimeTest {
    fun main(args: Array<String>) {
        var now = LocalDateTime.now().minusMonths(0).minusDays(1)//
        val weekId = 1
        (0..12).map { i ->
            (1..7).map { j ->
                now = now.minusDays(-1)
                now.format(DateTimeFormatter.ofPattern(TimeHelper.TimePattern.p2.pattern)).apply {
                    println("$this @研一下暑${2 + i}周${j}")
                }
            }
        }
    }
}