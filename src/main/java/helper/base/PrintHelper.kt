package helper.base

import helper.base.PrintHelper.StringTo.toPrint
import helper.base.PrintHelper.StringTo.toPrintln

object PrintHelper {
    object StringTo {
        fun String.toPrint() {
            print(this)
        }

        fun String.toPrintln() {
            println(this)
        }
    }

    object StringBuilderTo {
        fun StringBuilder.toPrint() {
            return this.toString().toPrint()
        }

        fun StringBuilder.toPrintln() {
            return this.toString().toPrintln()
        }
    }
}