package helper.base

object ToStrHelper {
    interface IToStr {
        fun toStr(
            tabNum: Int,
        ): String
    }

    object Expand {
        object ListE {
            object IToStrE {
                fun List<IToStr>.toStr(
                    tabNum: Int = 0,
                ): String {
                    val tabNumStr = "\t".repeat(tabNum)
                    val sb = StringBuilder()
                    this.map {
                        sb.append("${tabNumStr}${it.toStr(tabNum)}\n")
                    }
                    return sb.toString()
                }
            }

            object EE {
                fun <E> List<E>.toStr(
                    tabNum: Int = 0,
                ): String {
                    val tabNumStr = "\t".repeat(tabNum)
                    val sb = StringBuilder()
                    this.map {
                        sb.append("${tabNumStr}${it}\n")
                    }
                    return sb.toString()
                }
            }
        }

        object LHME {
            fun LinkedHashMap<String, String>.toStr(
                tabNum: Int = 0,
            ): String {
                val tabNum1 = tabNum + 1
                val tabNumStr = "\t".repeat(tabNum)
                val tabNumStr1 = "\t".repeat(tabNum1)
                val sb = StringBuilder()
                this.map { (k, v) ->
                    sb.append("${tabNumStr}${k}\n")
                    sb.append("${tabNumStr1}${v}\n")
                }
                return sb.toString()
            }
        }
    }
}