package helper.scxml

import org.apache.commons.scxml.model.Data

object IDataExpandHelper {
    interface IDataExpand {
        fun touchData(
            dataId: String,
            init: (Any) -> Unit,
        )

        fun getData(
            dataId: String,
        ): Any?

        fun setDataExpr(
            dataId: String,
            dataExpr: String,
        )

        fun setDataExprAddOne(
            dataId: String,
        )

        fun setDataExprZero(
            dataId: String,
        )

        fun ifDataExprEqualString(
            dataId: String,
            dataExpr: String,
        ): Boolean

        fun ifDataExprEqualInt(
            dataId: String,
            dataExpr: Int,
        ): Boolean {
            return ifDataExprEqualString(dataId, dataExpr.toString())
        }
    }

    object Expand {
        //不要再使用
        fun ScxmlVarHelper.ClockConstraint.ifMeet2(
            IDataExpand: IDataExpand,
        ): Boolean {
            return ifMeet(IDataExpand.getData(varId) as org.apache.commons.scxml2.model.Data)
        }
    }
}