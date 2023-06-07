package helper.base

import java.net.URL

object ResourceHelper {
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun getResource(
        string: String,
    ): URL {
        println("getResource():${string}")
        val resource = ResourceHelper::class.java.classLoader.getResource(
            string,
        )
        println("res:${resource}")
        return resource
    }

    val resDirPrefix = "src/main/resources"
    val resDirPrefix_t = "target/classes"
}