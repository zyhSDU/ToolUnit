package helper

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object XMLCreateHelper {
    fun getXMLStreamWriter(
        outputStream: OutputStream,
    ): XMLStreamWriter {
        return XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream)
    }

    fun getToFileXMLStreamWriter(
        outFile: String,
    ): XMLStreamWriter {
        return getXMLStreamWriter(FileOutputStream(outFile))
    }

    fun XMLStreamWriter.writeXMLFile(
        init: () -> Unit = {},
    ) {
        init()
        this.close()
    }

    fun XMLStreamWriter.writeDocument(
        init: () -> Unit = {},
    ) {
        this.writeStartDocument()
        init()
        this.writeEndDocument()
    }

    fun XMLStreamWriter.writeElement(
        localName: String,
        init: () -> Unit = {},
    ) {
        this.writeCharacters("\n")
        this.writeStartElement(localName)
        init()
        this.writeEndElement()
        this.writeCharacters("\n")
    }

    fun formatXML(f1: String, f2: String) {
        val xmlFile = File(f1)

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()

        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        val builderFactory = DocumentBuilderFactory.newInstance()
        val builder = builderFactory.newDocumentBuilder()
        val document = builder.parse(xmlFile)

        val source = DOMSource(document)
        val result = StreamResult(StringWriter())

        transformer.transform(source, result)

        result.writer.toString().let {
            File(f2).writeText(it)
        }
    }
}