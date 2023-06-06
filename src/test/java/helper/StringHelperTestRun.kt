package helper

import helper.base.CopyHelper.copyToClipboard
import helper.base.CopyHelper
import helper.base.PrintHelper.StringTo.toPrintln
import helper.base.ScannerHelper
import kotlin.reflect.KFunction1

object StringHelperTestRun {
    class ReUnit(
        val oldRes: String,
        var res: String,
    ) {
        fun re(
            initRe: (ReUnit) -> Unit
        ) {
            initRe(this)
            res.copyToClipboard()
            "${oldRes}\t==>\t${res}".toPrintln()
        }
    }

    fun String.toReUnit(): ReUnit {
        return ReUnit(this, this)
    }

    fun re1(
        reUnit: ReUnit,
    ) {
        reUnit.run {
            val range = 1998..2023
            res = res.replace("..", "_")
            res = res.replace(".", "")
            range.map {
                if (res.startsWith("@${it}")) {
                    res = res.replaceFirst("@${it}", "t_${it}_")
                } else if (res.startsWith("${it}")) {
                    res = res.replaceFirst("${it}", "t_${it}_")
                }
            }
            res = res.replace("-", "_")
            res = res.replace(" @", "_")
            res = res.replace("@", "_")
            res = res.replace(" ", "_")
            res = res.replace("__", "_")
        }
    }

    fun temp_re1(
        reUnit: ReUnit,
    ) {
        reUnit.run {
            res = res.replace("22 ", "t_2022_")
            res = res.replace("23 ", "t_2023_")
        }
    }

    fun task1(
        kFunction1: KFunction1<ReUnit, Unit>
    ) {
        ScannerHelper.map {
            CopyHelper.getFromClipboard().toReUnit().run {
                re(kFunction1)
            }
        }
    }

    fun KFunction1<ReUnit, Unit>.toTask1() {
        task1(this)
    }

    @JvmStatic
    fun main(args: Array<String>) {
//        ::temp_re1.toTask1()
        ::re1.toTask1()
    }
}

