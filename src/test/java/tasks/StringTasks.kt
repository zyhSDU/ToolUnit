package tasks

import helper.base.CopyHelper.copyToClipboard
import helper.base.PrintHelper.StringTo.toPrintln
import helper.base.ScannerHelper

object StringTasks {
    object ReplaceStr {
        @JvmStatic
        fun main(args: Array<String>) {
            ScannerHelper.mapTrim {
                val result = it.replace("¬ ", "-")
                result.toPrintln()
                result.copyToClipboard()
            }
        }
    }
    object SplitLine{
        @JvmStatic
        fun main(args: Array<String>) {
            ScannerHelper.mapTrim {
                val result = it.replace("。","。\n")
                result.toPrintln()
                result.copyToClipboard()
            }
        }
    }

    object JoinLine{
        @JvmStatic
        fun main(args: Array<String>) {

        }
    }

    object Task0523_01{
        @JvmStatic
        fun main(args: Array<String>) {
            "去除粗体".toPrintln()
            ScannerHelper.mapTrim {
                val result = it.replace("**","")
                result.toPrintln()
                result.copyToClipboard()
            }
        }
    }
}