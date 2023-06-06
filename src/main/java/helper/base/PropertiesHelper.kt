package helper.base

import java.io.InputStream
import java.util.*

object PropertiesHelper {
    // 新建Properties类的引用
    private var globalConf: Properties? = null
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun globalConf(): Properties? {
        if (globalConf == null) {
            globalConf = Properties() // Properties对象实例化
            // 通过类加载器获取配置文件字节流
            val rankConfStream: InputStream =
                PropertiesHelper::class.java.classLoader.getResourceAsStream("setting.properties")
            // 将配置文件装载到Properties类中
            globalConf?.load(rankConfStream)
        }
        return globalConf
    }

    fun getProperty(k: String): String {
        // 通过key-value的形式访问配置文件中对应的参数
        return globalConf()?.getProperty(k).toString()
    }
}