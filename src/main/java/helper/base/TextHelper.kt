package helper.base

import java.io.File

object TextHelper {
    fun String.toTextFile(fileName:String){
        File(fileName).writeText(this)
    }
}