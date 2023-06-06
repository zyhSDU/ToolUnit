package helper.scxml.scxml2

import helper.scxml.scxml2.Expand.DataExpand.setExprAddOne
import helper.scxml.scxml2.Expand.DataExpand.setExprToZero
import helper.scxml.scxml2.Expand.SCXMLExpand.toStr
import org.apache.commons.scxml2.model.Data
import org.apache.commons.scxml2.model.SCXML

class DataSCXML(
    val scxml: SCXML,
) : IDataExpandHelper.IDataExpand {
    val globalTimeData: Data = if (this.containsData(Res.globalTimeId)) {
        this.getData(Res.globalTimeId)!!
    } else {
        Data().also {
            it.id = Res.globalTimeId
            it.expr = "0"
            this.scxml.datamodel.addData(it)
        }
    }

    // 数据初始值
    val data0s = LinkedHashMap<String, String>().also { data0s ->
        scxml.datamodel.data.map {
            data0s[it.id] = it.expr
        }
    }

    override fun touchData(
        dataId: String,
        init: (Any) -> Unit,
    ) {
        this.scxml.datamodel.data.filterNotNull().map {
            if (it.id == dataId) {
                init(it)
                return@map
            }
        }
    }

    override fun getData(dataId: String): Data? {
        var data: Data? = null
        this.touchData(dataId) {
            it as Data
            data = it
        }
        return data
    }

    fun getDataInt(dataId: String): Int? {
        return getData(dataId)?.expr?.toDouble()?.toInt()
    }

    override fun setDataExpr(dataId: String, dataExpr: String) {
        this.touchData(dataId) {
            it as Data
            it.expr = dataExpr
        }
    }

    override fun setDataExprAddOne(dataId: String) {
        this.touchData(dataId) {
            it as Data
            it.setExprAddOne()
        }
    }

    override fun setDataExprZero(dataId: String) {
        this.touchData(dataId) {
            it as Data
            it.setExprToZero()
        }
    }

    override fun ifDataExprEqualString(
        dataId: String,
        dataExpr: String,
    ): Boolean {
        return getData(dataId)!!.expr.equals(dataExpr)
    }

    fun containsData(dataId: String): Boolean {
        var boolean = false
        this.touchData(dataId) {
            boolean = true
        }
        return boolean
    }

    fun reset() {
        this.scxml.datamodel.data.map {
            it.expr = data0s[it.id]
        }
    }

    fun toStr(
        tabNum: Int = 0,
    ): String {
        val tabNumStr = "\t".repeat(tabNum)
        val sb = StringBuilder()
        sb.append("${tabNumStr}dataSCXML:\n")
        sb.append(scxml.toStr(tabNum + 1))
        return sb.toString()
    }
}
