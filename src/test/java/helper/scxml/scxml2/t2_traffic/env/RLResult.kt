package helper.scxml.scxml2.t2_traffic.env

//直接写到文本上的，所以最好用字符串等存储
class RLResult(
    //计算的时候再换为Double
    var reward: Int = 0,
    var nextRLState: RLState = RLState(),
    var done: Boolean = false,
) {
    override fun toString(): String {
        return "${reward},${nextRLState},${done}"
    }
}