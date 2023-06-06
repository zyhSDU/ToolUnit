package helper.jacksonTest.test3

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.StringWriter

/**
 * bean转json格式或者json转bean格式, 项目中我们通常使用这个工具类进行json---java互相转化
 */
object JacksonUtil {
    private val mapper = ObjectMapper()
    fun bean2Json(obj: Any): String {
        val sw = StringWriter()
        val gen = JsonFactory().createGenerator(sw)
        mapper.writeValue(gen, obj)
        gen.close()
        return sw.toString()
    }

    fun <T> json2Bean(jsonStr: String, objClass: Class<T>): T {
        return mapper.readValue(jsonStr, objClass)
    }
}