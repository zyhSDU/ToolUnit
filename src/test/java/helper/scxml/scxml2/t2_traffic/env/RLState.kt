package helper.scxml.scxml2.t2_traffic.env

class RLState(
    var machineState: String = "",
    var dataGlobalTimeInt: Int = 0,
    var dataTInt: Int = 0,
    var retryTimes: Int = 0,
) : Cloneable {
    override fun toString(): String {
        return "(${machineState},${dataGlobalTimeInt},${dataTInt},${retryTimes})"
    }

    public override fun clone(): RLState {
        return RLState(machineState, dataGlobalTimeInt, dataTInt, retryTimes)
    }
}