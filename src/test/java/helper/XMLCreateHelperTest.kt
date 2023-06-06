package helper

import helper.XMLCreateHelper.writeDocument
import helper.XMLCreateHelper.writeElement
import helper.XMLCreateHelper.writeXMLFile
import helper.base.FileHelper
import org.junit.Test

internal class XMLCreateHelperTest {
    fun t1step1(
        outFile: String,
    ) {
        val it = XMLCreateHelper.getToFileXMLStreamWriter(outFile)
        it.writeXMLFile {
            it.writeDocument {
                it.writeElement("root") {
                    it.writeElement("person") {
                        it.writeAttribute("id", "1")
                        it.writeElement("name") {
                            it.writeCharacters("John")
                        }
                        it.writeElement("age") {
                            it.writeCharacters("30")
                        }
                    }
                    it.writeElement("person") {
                        it.writeAttribute("id", "2")
                        it.writeElement("name") {
                            it.writeCharacters("Jane")
                        }
                        it.writeElement("age") {
                            it.writeCharacters("25")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun t1() {
        val path = "out/xml/t1"
        FileHelper.createDirIfNotExists(path)
        val exampleXMLPath1 = "${path}/example1.xml"
        val exampleXMLPath2 = "${path}/example2.xml"
        t1step1(exampleXMLPath1)
        XMLCreateHelper.formatXML(exampleXMLPath1, exampleXMLPath2)
    }
}