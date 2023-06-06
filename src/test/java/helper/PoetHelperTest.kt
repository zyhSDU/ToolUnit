package helper

import com.squareup.kotlinpoet.*
import org.junit.Test
import java.io.File


internal class PoetHelperTest{
    @Test
    fun t1(){
        val greetingFun = FunSpec.builder("greeting")
            .addStatement("println(\"Hello, world!\")")
            .build()

        val myClass = TypeSpec.classBuilder("MyClass")
            .addFunction(greetingFun)
            .build()

        val file = FileSpec.builder("com.example", "MyClass")
            .addType(myClass)
            .build()

        val writer = File("MyClass.kt").writer()
        file.writeTo(writer)
        writer.close()

    }
}