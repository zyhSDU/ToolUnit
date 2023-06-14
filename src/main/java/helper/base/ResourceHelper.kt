package helper.base

import helper.base.DebugHelper.DebuggerList
import helper.base.DebugHelper.getDebuggerList
import java.net.URL

object ResourceHelper {
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun getResource(
        string: String,
        debuggerList: DebuggerList = getDebuggerList(0),
    ): URL {
        debuggerList.pln("getResource():${string}")
        val resource = ResourceHelper::class.java.classLoader.getResource(
            string,
        )
        debuggerList.pln("res:${resource}")
        return resource
    }

    val resDirPrefix = "src/main/resources"
    val resDirPrefix_t = "target/classes"
}