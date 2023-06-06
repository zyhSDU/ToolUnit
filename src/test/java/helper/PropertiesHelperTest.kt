package helper

import helper.base.PropertiesHelper
import org.junit.Test

internal class PropertiesHelperTest {
    @Test
    fun t1() {
        println(PropertiesHelper.getProperty("firstViewIndex"))
    }
}