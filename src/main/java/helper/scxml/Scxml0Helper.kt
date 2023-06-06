package helper.scxml

import org.apache.commons.scxml.model.Data

object Scxml0Helper {
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
        fun ScxmlVarHelper.ClockConstraint.ifMeet1(
            IDataExpand: IDataExpand,
        ): Boolean {
            return ifMeet(IDataExpand.getData(varId) as Data)
        }

        fun ScxmlVarHelper.ClockConstraint.ifMeet2(
            IDataExpand: IDataExpand,
        ): Boolean {
            return ifMeet(IDataExpand.getData(varId) as org.apache.commons.scxml2.model.Data)
        }
    }
}