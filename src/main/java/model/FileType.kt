package model

enum class FileType(val suffix: String) {
    PNG(".png"),
    JPG(".jpg"),
    TXT(".txt"),
    JAVA(".java"),
    KT(".kt"),
    ;

    enum class FileTypeGroup(val arrayList: ArrayList<FileType>) {
        Image(arrayListOf(PNG, JPG)),
        Text(arrayListOf(TXT, JAVA, KT)),
        ;

        fun doOne(name: String, one: () -> Unit) {
            arrayList.map {
                if (name.endsWith(it.suffix)) {
                    one()
                }
            }
        }
    }
}