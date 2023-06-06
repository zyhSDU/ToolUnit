package helper.base

import java.util.*

object ScannerHelper {
    private val scanner = Scanner(System.`in`)

    //不考虑多线程
    fun map(
        init: (String) -> Unit,
    ) {
        while (scanner.hasNext()) {
            val nextLine = scanner.nextLine()
            init(nextLine)
        }
    }

    fun mapTrim(
        init: (String) -> Unit,
    ) {
        map {
            it.run {
                trim()
            }.run {
                init(this)
            }
        }
    }
}