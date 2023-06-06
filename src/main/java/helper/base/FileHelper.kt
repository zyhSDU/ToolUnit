package helper.base

import java.io.File

object FileHelper {
    fun createFileIfNotExists(file: File) {
        if (file.exists()) return
        file.parentFile.mkdirs()
        file.createNewFile()
    }

    fun createFileIfNotExists(fileName: String) {
        createFileIfNotExists(File(fileName))
    }

    fun createDirIfNotExists(file: File) {
        file.mkdirs()
    }

    fun createDirIfNotExists(fileName: String) {
        createDirIfNotExists(File(fileName))
    }

    fun String.toFile() = File(this)

    //创建dir
    //createDirIfNotExists
    fun String.mkdirs() {
        this.toFile().mkdirs()
    }

    fun getNewMaxIndexDirFileString(
        dirString: String,
        fileString: String,
    ): String {
        var index = 0
        while (true) {
            val returnFileString = "${dirString}\\${fileString}${index}"
            val file = File(returnFileString)
            if (file.exists()) {
                index += 1
            } else {
                return returnFileString
            }
        }
    }
}