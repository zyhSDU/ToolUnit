package helper.scxml.scxml2

import helper.XMLCreateHelper
import helper.base.ResourceHelper
import helper.scxml.IDataExpandHelper
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.Expand.DataExpand.setExprAddOne
import helper.scxml.scxml2.Expand.DataExpand.setExprToZero
import helper.scxml.scxml2.Expand.TransitionTargetExpand.doEntriesAssign
import helper.scxml.scxml2.Expand.TransitionTargetExpand.doExitsAssign
import helper.scxml.scxml2.Expand.XMLStreamWriterExpand.writeTransitionTargetToScxml
import helper.scxml.scxml2.Expand.XMLStreamWriterExpand.writeTransitionTargetToT
import org.apache.commons.scxml2.Evaluator
import org.apache.commons.scxml2.SCXMLExecutor
import org.apache.commons.scxml2.env.SimpleErrorReporter
import org.apache.commons.scxml2.env.jexl.JexlEvaluator
import org.apache.commons.scxml2.io.SCXMLReader
import org.apache.commons.scxml2.model.Data
import org.apache.commons.scxml2.model.Transition
import org.apache.commons.scxml2.model.TransitionTarget
import java.io.ByteArrayOutputStream
import java.io.File

object Scxml2Helper {
    fun getStringFromXML(
        resDir: String,
        ppString: String,
        prefixString: String,
        indexString: String,
    ): String {
        val outputStream = ByteArrayOutputStream()
        val xmlStreamWriter = XMLCreateHelper.getXMLStreamWriter(outputStream)
        val scxmlName = "${prefixString}_${indexString}"
        val scxmlTuple = getSCXMLTuple("${resDir}/${scxmlName}.scxml")
        xmlStreamWriter.writeTransitionTargetToT(
            resDir,
            ppString,
            scxmlName,
            scxmlTuple.dataSCXML.scxml,
            ArrayList(),
        )
        return outputStream.toString()
    }

    fun createScxml(
        resDir: String,
        oldStateName: String,
        newStateIndex: String,
        statePrefixList: ArrayList<String> = ArrayList(),
    ) {
        val newFile = File("${ResourceHelper.resDirPrefix_t}/${resDir}/${oldStateName}_${newStateIndex}.scxml")
        if (newFile.exists()) return

        val outputStream = ByteArrayOutputStream()
        val xmlStreamWriter = XMLCreateHelper.getXMLStreamWriter(outputStream)
        val scxmlTuple = getSCXMLTuple("${resDir}/${oldStateName}.scxml")
        xmlStreamWriter.writeTransitionTargetToScxml(
            resDir,
            "${oldStateName}_${newStateIndex}",
            scxmlTuple.dataSCXML.scxml,
            statePrefixList,
        )
        outputStream.toString()
            .replace("""<![CDATA[""", "")
            .replace("""]]>""", "")
            .let {
                newFile.writeText(it)
            }
    }

    fun getStateString(
        resDir: String,
        ppString: String,
        statePrefix: String,
        stateIndex: String,
        statePrefixList: ArrayList<String> = ArrayList(),
    ): String {
        val newFile = File("${ResourceHelper.resDirPrefix_t}/${resDir}/${statePrefix}_${stateIndex}.scxml")
        if (!newFile.exists()) {
            createScxml(
                resDir,
                statePrefix,
                stateIndex,
                statePrefixList,
            )
        }
        return getStringFromXML(resDir, ppString, statePrefix, stateIndex)
    }

    data class StateTransitionEventUnit(
        val stateId: String,
        val transition: Transition,
        val event: String,
        val cond: ClockConstraint?,
    ) {
        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("(${stateId}),")
            sb.append("(${event}),")
            sb.append("(${cond}),")
            sb.append("(${transition.targets.joinToString(",") { it.id }})")
            return sb.toString()
        }
    }

    object TransitionTargetStatus {
        const val entered = 0
        const val running = 1
        const val finished = 2
    }

    class MyDataList(
        val dataLHM: LinkedHashMap<String, Data> = LinkedHashMap(),
    ) : IDataExpandHelper.IDataExpand {
        override fun touchData(
            dataId: String,
            init: (Any) -> Unit,
        ) {
            init(dataLHM[dataId]!!)
        }

        override fun getData(dataId: String): Data? {
            var data: Data? = null
            this.touchData(dataId) {
                it as Data
                data = it
            }
            return data
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
    }

    class StateTreeNode(
        val tt: TransitionTarget?,
        val tf: StateTreeNode? = null,
        val children: ArrayList<StateTreeNode> = ArrayList(),
    ) {
        companion object {
            fun getRootStateTreeNode(): StateTreeNode {
                return StateTreeNode(null)
            }
        }

        fun fatherFirstTouch(
            init: (StateTreeNode) -> Unit,
        ) {
            init(this)
            this.children.map {
                it.fatherFirstTouch(init)
            }
        }

        fun ifRootStateTreeNode(): Boolean {
            return this.tt == null && children.isEmpty()
        }

        fun addChild(
            tt: StateTreeNode,
        ) {
            this.children.add(tt)
        }

        fun addChild(
            tt: TransitionTarget,
        ) {
            this.addChild(
                StateTreeNode(
                    tt,
                    this,
                )
            )
        }

        fun addChildAndReturnChild(
            tt: TransitionTarget,
        ): StateTreeNode {
            val stateTreeNode = StateTreeNode(
                tt,
                this,
            )
            this.addChild(
                stateTreeNode
            )
            return stateTreeNode
        }


        fun removeChild(
            stn: StateTreeNode,
        ) {
            this.children.remove(stn)
        }

        override fun toString(): String {
            return "(${tt?.id}${
                children.joinToString("") {
                    ",$it"
                }
            })"
        }
    }

    class ActiveStateTreeNode(
        val tt: TransitionTarget?,
        val tf: ActiveStateTreeNode? = null,
        val children: ArrayList<ActiveStateTreeNode> = ArrayList(),
        var transitionTargetStatus: Int = TransitionTargetStatus.entered,
    ) {
        companion object {
            fun getRootStateTreeNode(): ActiveStateTreeNode {
                return ActiveStateTreeNode(null)
            }
        }

        fun fatherFirstTouch(
            init: (ActiveStateTreeNode) -> Unit,
        ) {
            init(this)
            this.children.map {
                it.fatherFirstTouch(init)
            }
        }

        fun childrenFirstTouch(
            init: (ActiveStateTreeNode) -> Boolean,
        ): Boolean {
            var ifSkip = false
            children.map {
                if (it.childrenFirstTouch(init)) {
                    if (!ifSkip) {
                        ifSkip = true
                    }
                }
            }
            if (!ifSkip) {
                init(this)
            }
            return ifSkip
        }

        fun ifRootStateTreeNode(): Boolean {
            return this.tt == null && children.isEmpty()
        }

        fun ifRootStateTreeNodeToEnd(): Boolean {
            return this.ifRootStateTreeNode()
                    && transitionTargetStatus == TransitionTargetStatus.finished
        }

        fun ifRootStateTreeNodeToStart(): Boolean {
            return this.ifRootStateTreeNode()
                    && transitionTargetStatus == TransitionTargetStatus.entered
        }

        fun addChildAndDo(
            tt: TransitionTarget,
            myDataList: MyDataList? = null,
        ) {
            transitionTargetStatus = TransitionTargetStatus.running
            this.children.add(
                ActiveStateTreeNode(
                    tt,
                    this,
                    transitionTargetStatus = TransitionTargetStatus.entered,
                )
            )
            if (myDataList != null) {
                tt.doEntriesAssign(myDataList)
            }
        }

        fun removeChildAndDo(
            stn: ActiveStateTreeNode,
            myDataList: MyDataList? = null,
        ) {
            if (myDataList != null) {
                stn.tt!!.doExitsAssign(myDataList)
            }
            this.children.remove(stn)
            tf!!.transitionTargetStatus = TransitionTargetStatus.finished
        }

        fun contains(id: String): Boolean {
            var boolean = false
            this.fatherFirstTouch {
                if (!boolean) {
                    if (it.tt != null) {
                        if (it.tt.id == id) {
                            boolean = true
                        }
                    }
                }
            }
            return boolean
        }

        override fun toString(): String {
            return "(${tt?.id}${
                children.joinToString("") {
                    ",$it"
                }
            })"
        }
    }

    fun getSCXMLTuple(scxmlPath: String): SCXMLTuple {
        //实例化数据模型解析器
        val evaluator: Evaluator = JexlEvaluator()

        //实例化引擎
        val executor = SCXMLExecutor(evaluator, null, SimpleErrorReporter())

        //加载资源文件,实例化到一个SCXML对象，两者之间一一对应
        val scxml = SCXMLReader.read(ResourceHelper.getResource(scxmlPath))

        //将这样的一个SCXML实例，作为状态机对象，传入到引擎里面。
        executor.stateMachine = scxml

        return SCXMLTuple(evaluator, executor, scxml)
    }
}