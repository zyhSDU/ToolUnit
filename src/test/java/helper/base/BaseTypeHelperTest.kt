package helper.base

import helper.base.BaseTypeHelper.LHMExpand.StringIntExpand.getMaxKey
import org.junit.Test

internal class BaseTypeHelperTest {
    @Test
    fun test_StringDoubleExpand() {
        val map: LinkedHashMap<String, Int> = LinkedHashMap()
        // 假设已经初始化了该 LinkedHashMap

        map["key1"] = 10
        map["key2"] = 20
        map["key3"] = 20

        val maxKey = map.getMaxKey()
        println("Key with maximum value: $maxKey")
        assert(maxKey == "key3")
    }
}