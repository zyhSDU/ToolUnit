package helper.scxml.scxml2.t3_factory

import helper.scxml.scxml2.Scxml2Helper
import org.junit.Test

internal class FactoryTest {
    @Test
    fun t1() {
        Scxml2Helper.createScxml(
            "scxml2/t_factory",
            "platform",
            "1",
            arrayListOf("switch", "axis"),
        )
    }

    @Test
    fun t2() {
        Scxml2Helper.createScxml(
            "scxml2/t_math",
            "f",
            "1",
            arrayListOf("m","d1","d2",),
        )
    }
}