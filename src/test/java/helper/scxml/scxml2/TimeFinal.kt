package helper.scxml.scxml2

import helper.scxml.scxml2.Expand.SCXMLExpand.touchTransitionTarget
import org.apache.commons.scxml2.model.Final
import org.apache.commons.scxml2.model.State
import org.junit.Test

internal class TimeFinal {
    @Test
    fun testFinal() {
        val scxmlTuple = Scxml2Helper.getSCXMLTuple("scxml2/test_final.scxml")
        scxmlTuple.dataSCXML.scxml.touchTransitionTarget { tf, tt ->
            println(tt.id)
            if (tt is Final) {
                println("\tis Final")
            }
            if (tt is State) {
                println("\tis State")
                println("\t${tt.first}")
            }
        }
    }
}