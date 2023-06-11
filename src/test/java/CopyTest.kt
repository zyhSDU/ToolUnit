import org.junit.Test

internal class CopyTest {
    @Test
    fun t1() {
        data class Person(
            val name: String,
            val age: Int,
            val addresses: MutableList<String>,
        )

        val person = Person("John", 30, mutableListOf("New York", "London"))
        val newPerson1 = person.copy(addresses = mutableListOf("Paris"))
        val newPerson2 = person.copy()
        newPerson2.addresses.add("hhhh")

        val sb = StringBuilder()
        sb.append("${person}\n")
        sb.append("${newPerson1}\n")
        sb.append("${newPerson2}\n")
        val rs = sb.toString().trim()
        println(rs)
        assert(
            rs == """
Person(name=John, age=30, addresses=[New York, London, hhhh])
Person(name=John, age=30, addresses=[Paris])
Person(name=John, age=30, addresses=[New York, London, hhhh])
        """.trimIndent().trim()
        )
    }
}