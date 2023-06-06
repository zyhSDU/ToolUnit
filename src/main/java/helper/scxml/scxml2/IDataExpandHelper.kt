package helper.scxml.scxml2

import helper.scxml.IDataExpandHelper
import helper.scxml.ScxmlVarHelper
import org.apache.commons.scxml2.model.Data

object IDataExpandHelper {
    interface IDataExpand : IDataExpandHelper.IDataExpand {
        override fun getData(
            dataId: String,
        ): Data?
    }

    object Expand {
        fun ScxmlVarHelper.ClockConstraint.ifMeet(
            dataExpand: IDataExpand,
        ): Boolean {
            return ifMeet(dataExpand.getData(varId)!!)
        }
    }
}