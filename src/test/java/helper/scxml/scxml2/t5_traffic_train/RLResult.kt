package helper.scxml.scxml2.t5_traffic_train

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