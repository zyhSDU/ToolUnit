package helper.jacksonTest.test3

class Book {
    //书的ID
    var bookId = 0

    //作者
    var author = ""

    //书名
    var name = ""

    //书价
    var price = 0
    override fun toString(): String {
        return "Book(bookId=$bookId, author='$author', name='$name', price=$price)"
    }
}