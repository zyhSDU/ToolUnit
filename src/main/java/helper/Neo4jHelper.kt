package helper

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Label
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import java.io.File
import java.util.*

object Neo4jHelper {
    class Neo4jNode(var lables: List<String>, var properties: HashMap<String, String> = HashMap())

    class Neo4jRelationship(var types: List<String>, var properties: HashMap<String, String> = HashMap())

    val dbPath = "D:\\Users\\zyh\\softs\\neo4j\\neo4j-community-3.4.0-windows\\data\\databases\\graph.db"
    val graphDatabaseService = GraphDatabaseFactory().newEmbeddedDatabase(File(dbPath))

    fun createNode(neo4jNode: Neo4jNode) {
        graphDatabaseService.doIt { db ->
            neo4jNode.apply {
                lables.map { Label { it } }.toTypedArray().run {
                    db.createNode(*this)
                }.run {
                    properties.map {
                        setProperty(it.key, it.value)
                    }
                }
            }
        }
    }

    private fun GraphDatabaseService.doIt(a: (db: GraphDatabaseService) -> Unit) {
        beginTx().use { tx ->
            a(this)
            tx.success()
            tx.close()
        }
    }

    object Test {
        fun createNodeTest() {
            Neo4jNode(arrayListOf("dc")).apply {
                properties["-name"] = "state"
            }.run {
                createNode(this)
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Test.createNodeTest()
//        selectNode(Label { "单词" }).run {
//            this.map {
//                println(it)
//            }
//        }
//        selectNode(Label { "单词" }, "-name", "state").run {
//            this.map {
//                println(it)
//            }
//        }
    }
}