package helper.jacksonTest.test2;

public class Book {
    private int bookId;//书的ID
    private String author;//作者
    private String name;//书名
    private int price;//书价

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Book [bookId=" + bookId + ", author=" + author + ", name="
                + name + ", price=" + price + "]";
    }

}