package helper.scxml.scxml1

import helper.scxml.IDataExpandHelper
import helper.scxml.ScxmlVarHelper
import org.apache.commons.scxml.model.Data

object IDataExpandHelper {
    interface IDataExpand : IDataExpandHelper.IDataExpand {
        override fun getData(
            dataId: String,
        ): Data?
    }

    object Expand {
        fun ScxmlVarHelper.ClockConstraint.ifMeet(
            IDataExpand: IDataExpand,
        ): Boolean {
            return ifMeet(IDataExpand.getData(varId)!!)
        }
    }
}