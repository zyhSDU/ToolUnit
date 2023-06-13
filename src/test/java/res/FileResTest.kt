package res

import org.junit.Test

internal class FileResTest{
    @Test
    fun t1(){
        println(FileRes.root_file.absolutePath)
        println(FileRes.document_file.absolutePath)
        println(FileRes.d_a1_File.absolutePath)
        println(FileRes.d_a2_File.absolutePath)
    }
}