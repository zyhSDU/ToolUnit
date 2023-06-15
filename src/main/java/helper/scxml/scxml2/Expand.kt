package helper.scxml.scxml2

import helper.XMLCreateHelper.writeElement
import helper.base.DebugHelper.DebuggerList
import helper.base.DebugHelper.DebuggerList.Companion.getDebuggerList
import helper.base.LHMHelper
import helper.base.RegexHelper.match
import helper.base.RegexHelper.matchPrefix
import helper.block.ScxmlBlockHelper
import helper.scxml.IDataExpandHelper.IDataExpand
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.scxml2.Expand.ExecutableExpand.doExecutable
import helper.scxml.scxml2.Expand.ToStr.TCLHMExpand.toStr
import helper.scxml.scxml2.Expand.TransitionExpand.toStr
import helper.scxml.scxml2.Expand.TransitionTargetExpand.toStr
import helper.scxml.scxml2.Expand.TransitionTargetExpand.touchTransitionTarget
import helper.scxml.scxml2.Scxml2Helper.StateTransitionEventUnit
import helper.scxml.strategy.ScxmlOneStrategyHelper
import org.apache.commons.scxml2.SCXMLExecutor
import org.apache.commons.scxml2.TriggerEvent
import org.apache.commons.scxml2.model.*
import javax.xml.stream.XMLStreamWriter
import kotlin.math.max
import kotlin.math.min

object Expand {
    fun String.toSignalEvent(): TriggerEvent {
        return TriggerEvent(this, TriggerEvent.SIGNAL_EVENT)
    }

    object ToBlockClass {
        fun Data.toBlockData(): ScxmlBlockHelper.ScxmlBlockFactory.Data {
            return ScxmlBlockHelper.ScxmlBlockFactory.Data(this.id, this.expr)
        }

        fun Datamodel.toBlockDataModel(): ScxmlBlockHelper.ScxmlBlockFactory.DataModel {
            val resultData = ScxmlBlockHelper.ScxmlBlockFactory.DataModel()
            this.data.filterNotNull().map {
                resultData.dataList.add(it.toBlockData())
            }
            return resultData
        }

        fun TransitionTarget.toBlockState(): ScxmlBlockHelper.ScxmlBlockFactory.BlockState {
            return ScxmlBlockHelper.ScxmlBlockFactory.BlockState(id = this.id)
        }
    }

    object DataExpand {
        fun Data.ifExprEqualsInt(
            int: Int,
        ): Boolean {
            return this.expr.equals("$int")
        }

        fun Data.exprToInt(): Int {
            return this.expr.toInt()
        }

        fun Data.setExprAddIncrement(
            increment: Int,
        ) {
            this.expr = "${this.exprToInt() + increment}"
        }

        fun Data.setExprAddOne() {
            this.setExprAddIncrement(1)
        }

        fun Data.setExprToZero() {
            this.expr = "0"
        }
    }

    object ExecutableExpand {
        fun Executable.doExecutable(
            IDataExpand: IDataExpand,
            debuggerList: DebuggerList = getDebuggerList(0),
        ) {
            this.actions.filterNotNull().map { action ->
                if (action is Assign) {
                    debuggerList.pln("action.expr=${action.expr}")
                    action.expr.let actionExpr@{ actionExpr: String ->
                        if (actionExpr == "0") {
                            IDataExpand.setDataExprZero(action.location)
                            return@actionExpr
                        }
                        actionExpr.match(Regex("(${action.location})(?=\\s*\\+\\s*1)"))?.let {
                            IDataExpand.setDataExprAddOne(action.location)
                            return@actionExpr
                        }
                        IDataExpand.setDataExpr(action.location, actionExpr)
                    }
                }
            }
        }
    }

    object TransitionTargetExpand {
        fun TransitionTarget.toStr(): String {
            return id
        }

        fun TransitionTarget.touchTransitionTarget(
            tf: TransitionTarget?,
            init: (TransitionTarget?, TransitionTarget) -> Unit,
        ) {
            init(tf, this)
            when (this) {
                is State -> {
                    this.children.map {
                        it.touchTransitionTarget(this, init)
                    }
                }
                is Parallel -> {
                    this.children.map {
                        it.touchTransitionTarget(this, init)
                    }
                }
            }
        }

        fun TransitionTarget.touchFromRootToThis(
            init: (TransitionTarget) -> Unit = {},
        ) {
            if (this.parent != null) {
                this.parent.touchFromRootToThis(init)
            }
            init(this)
        }

        private fun TransitionTarget.doEAssign(
            scxml: IDataExpand,
            es: List<Executable>,
            debuggerList: DebuggerList = getDebuggerList(0),
        ) {
            if (this !is EnterableState) return
            es.map {
                it.doExecutable(scxml, debuggerList)
            }
        }

        fun TransitionTarget.doExitsAssign(
            scxml: IDataExpand,
            debuggerList: DebuggerList = getDebuggerList(0),
        ) {
            if (this !is EnterableState) return
            this.doEAssign(scxml, this.onExits, debuggerList)
        }

        fun TransitionTarget.doEntriesAssign(
            scxml: IDataExpand,
            debuggerList: DebuggerList = getDebuggerList(0),
        ) {
            if (this !is EnterableState) return
            this.doEAssign(scxml, this.onEntries, debuggerList)
        }

        fun TransitionTarget.calculateExpectation(
            scxmlTuple: SCXMLTuple,
            envStrategyNode: ScxmlOneStrategyHelper.SNode,
            clocks: LinkedHashMap<String, Data>,
            globalTimeMax: Int,
        ): Double {
            // 全局时间
            val gTimeData = clocks[Res.globalTimeId]!!
            val gTime = gTimeData.expr.toInt()
            // 可能不只需要返回时间，其他参数值也可能要
            if (this is Final) return gTime.toDouble()
            var res = 0.0
            assert(this is TransitionalState)
            this as TransitionalState
            (this.transitionsList.size > 0).let {
                if (it) {
                    // 只取第一个转换？
                    this.transitionsList[0]
                } else {
                    null
                }
            }?.let {
                scxmlTuple.transitionCondLHM[it]
            }?.let { clockConstraint: ClockConstraint ->
                val aTimeData = clocks[clockConstraint.varId]!!
                val aTime = aTimeData.expr.toInt()
                val aTime1 = max(aTime, clockConstraint.minV)
                val aTime2 = min(globalTimeMax - gTime + aTime, clockConstraint.maxV)
                val pCount = aTime2 - aTime1 + 1
                if (pCount < 0) {
                    return res
                }
                val resArr = Array(pCount) { 0.0 }
                (aTime1..aTime2).map { aTimeNew ->
                    aTimeData.expr = aTimeNew.toString()
                    gTimeData.expr = (aTimeNew - aTime + gTime).toString()
                    ScxmlOneStrategyHelper.getStrategyLeafNode(
                        envStrategyNode = envStrategyNode,
                        IDataExpand = scxmlTuple.dataSCXML,
                        filterStateFun = { it.size == 1 && it.toList()[0] == this.id },
                    )?.let { sNode ->
                        sNode.eventDPLHM!!.let { eDPLHM ->
                            val eDPLHMSum = eDPLHM.values.sum()
                            eDPLHM.map { (event, p) ->
                                //1个event可能对应多个transition，暂时先取第0个，1个transition可能对应多个target，暂时取第0个
                                scxmlTuple.eventUnitLHM[event]!![this.id]!![0].transition.targets.toList()[0].let { t: TransitionTarget ->
                                    scxmlTuple.doExecutable(event)
                                    //这里的进入下一层计算，可能存在问题，因为没有复制创建新格局
                                    resArr[aTimeNew - aTime1] += (p / eDPLHMSum) * t.calculateExpectation(
                                        scxmlTuple,
                                        envStrategyNode,
                                        clocks,
                                        globalTimeMax,
                                    )
                                }
                            }
                        }
                    }
                }
                res = resArr.sum() / pCount
                aTimeData.expr = aTime.toString()
                gTimeData.expr = gTime.toString()
            }
            return res
        }
    }

    object EnterableStateExpand {
        fun List<EnterableState>.toStateTreeNode(
            rootStateTreeNode: Scxml2Helper.StateTreeNode,
        ): Scxml2Helper.StateTreeNode {
            val returnSTN = Scxml2Helper.StateTreeNode(null, null)
            val lhm = LinkedHashMap<Scxml2Helper.StateTreeNode, Scxml2Helper.StateTreeNode>()
            lhm[rootStateTreeNode] = returnSTN
            rootStateTreeNode.fatherFirstTouch {
                it.children.map { child: Scxml2Helper.StateTreeNode ->
                    if (this.contains(child.tt)) {
                        lhm[child] = lhm[it]!!.addChildAndReturnChild(child.tt!!)
                    }
                }
            }
            return returnSTN
        }
    }

    object TransitionExpand {
        fun Transition.toStr(): String {
            val sb = StringBuilder()
            sb.append("transition(")
            sb.append("event=${event},")
            sb.append("targets=[${
                targets.joinToString {
                    it.id
                }
            }]")
            sb.append(")")
            return sb.toString()
        }
    }

    object SCXMLExpand {
        fun SCXML.touchTransitionTarget(
            init: (TransitionTarget?, TransitionTarget) -> Unit = { tf, tt -> },
        ) {
            this.children.map {
                it.touchTransitionTarget(null, init)
            }
        }

        //Transition 的 cond 在 SCXMLTuple 中被去掉
        fun SCXML.touchTransition(
            init: (TransitionTarget, Transition) -> Unit,
        ) {
            this.touchTransitionTarget { _, tt ->
                if (tt is TransitionalState) {
                    tt.transitionsList.filterNotNull().map { t ->
                        init(tt, t)
                    }
                }
            }
        }

        fun SCXML.getInitialState(): EnterableState {
            this.children.map {
                if (it.id == this.initial) {
                    return it
                }
            }
            return this.firstChild
        }

        fun SCXML.getIdStateLHM(): LinkedHashMap<String, TransitionTarget> {
            val lhm = LinkedHashMap<String, TransitionTarget>()
            this.touchTransitionTarget { _, tt ->
                lhm[tt.id] = tt
            }
            return lhm
        }

        //太复杂，放弃
        fun SCXML.dfsTouchTransitionTargetStr(
            path: ArrayList<TransitionTarget>,
            sb: StringBuilder = StringBuilder(),
        ): String {
            return sb.toString()
        }

        fun SCXML.toStr(
            tabNum: Int = 0,
        ): String {
            val tabNum1 = tabNum + 1
            val tabNum2 = tabNum + 2
            val tabNumStr = "\t".repeat(tabNum)
            val tabNumStr1 = "\t".repeat(tabNum1)
            val tabNumStr2 = "\t".repeat(tabNum2)
            val sb = StringBuilder()
            sb.append("${tabNumStr}scxml:\n")
            sb.append("${tabNumStr1}touchTransitionTarget:\n")
            this.touchTransitionTarget { _, tt ->
                sb.append("${tabNumStr2}${tt.id}\n")
            }
            sb.append("${tabNumStr1}touchTransition:\n")
            this.touchTransition { tt, t ->
                sb.append("${tabNumStr2}${tt.id}\t${t.toStr()}\n")
            }
            return sb.toString()
        }
    }

    object SCXMLExecutorExpand {
        fun SCXMLExecutor.isInState(stateId: String): Boolean {
            return this.status.isInState(stateId)
        }
    }

    object LHMExpand {
        fun LinkedHashMap<String, TransitionTarget>.addState(
            state: TransitionTarget,
        ) {
            this[state.id] = state
        }

        fun LinkedHashMap<String, Data>.toStringDoubleLHM(
        ): LinkedHashMap<String, Double> {
            val lhm = LinkedHashMap<String, Double>()
            this.map {
                lhm[it.key] = it.value.expr.toDouble()
            }
            return lhm
        }
    }

    object XMLStreamWriterExpand {
        fun XMLStreamWriter.writeTransition(
            prefixString: String,
            t: Transition,
        ) {
            this.writeElement("transition") {
                this.writeAttribute(
                    "event",
                    "${prefixString}_${t.event}",
                )
                this.writeAttribute(
                    "target",
                    "${prefixString}_${t.targets.toList()[0].id}",
                )
            }
        }

        fun XMLStreamWriter.writeTransitionTarget(
            resDir: String,
            prefixString: String,
            tt: TransitionTarget,
            statePrefixList: ArrayList<String>,
        ) {
            statePrefixList.map { i_prefix ->
                tt.id.matchPrefix(i_prefix)?.let { i_index ->
                    Scxml2Helper.getStateString(
                        resDir,
                        prefixString,
                        i_prefix,
                        i_index,
                        statePrefixList,
                    ).let {
                        this.writeCData("\n${it}")
                    }
                    return
                }
            }
            this.writeElement("state") {
                this.writeAttribute(
                    "id",
                    "${prefixString}_${tt.id}",
                )
                if (tt is State) {
                    if (tt.first != null) {
                        this.writeAttribute(
                            "initial",
                            "${prefixString}_${tt.first}",
                        )
                    }
                }
                if (tt is TransitionalState) {
                    tt.children.map {
                        this.writeTransitionTarget(
                            resDir,
                            prefixString,
                            it,
                            statePrefixList,
                        )
                    }
                    tt.transitionsList.map { transition ->
                        this.writeTransition(
                            prefixString,
                            transition,
                        )
                    }
                }
                if (tt is EnterableState) {
                    if (tt.onEntries.size > 0) {
                        this.writeElement("onentry") {
                            tt.onEntries[0].actions.map {
                                if (it is Assign) {
                                    this.writeElement("assign") {
                                        this.writeAttribute("expr", it.expr)
                                        this.writeAttribute("location", it.location)
                                    }
                                }
                            }
                        }
                    }
                    if (tt.onExits.size > 0) {
                        this.writeElement("onexit") {
                            tt.onExits[0].actions.map {
                                if (it is Assign) {
                                    this.writeElement("assign") {
                                        this.writeAttribute("expr", it.expr)
                                        this.writeAttribute("location", it.location)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        fun XMLStreamWriter.writeTransitionTargetToT(
            resDir: String,
            prefixString: String,
            scxmlName: String,
            tt: SCXML,
            statePrefixList: ArrayList<String>,
        ) {
            this.writeElement("state") {
                this.writeAttribute(
                    "id",
                    "${prefixString}_${scxmlName}",
                )
                this.writeAttribute(
                    "initial",
                    "${prefixString}_${tt.initial}",
                )
                tt.children.map {
                    this.writeTransitionTarget(
                        resDir,
                        prefixString,
                        it,
                        statePrefixList,
                    )
                }
            }
        }

        fun XMLStreamWriter.writeTransitionTargetToScxml(
            resDir: String,
            prefixString: String,
            tt: SCXML,
            statePrefixList: ArrayList<String>,
        ) {
            this.writeElement("scxml") {
                this.writeAttribute(
                    "initial",
                    "${prefixString}_${tt.initial}",
                )
                this.writeAttribute(
                    "version",
                    "1.0"
                )
                this.writeAttribute(
                    "xmlns",
                    "http://www.w3.org/2005/07/scxml"
                )
                this.writeElement("datamodel") {
                    tt.datamodel.data.map {
                        this.writeElement("data") {
                            this.writeAttribute("expr", it.expr)
                            this.writeAttribute("id", it.id)
                        }
                    }
                }
                tt.children.map {
                    this.writeTransitionTarget(
                        resDir,
                        prefixString,
                        it,
                        statePrefixList,
                    )
                }
            }
        }
    }

    object ToStr {
        fun LinkedHashMap<String, TransitionTarget>.toStr(
            tabNum: Int = 0,
        ): String {
            if (tabNum < 0) throw IllegalArgumentException()
            val tabNumStr = "\t".repeat(tabNum)
            val sb = StringBuilder()
            this.map { (k, v) ->
                sb.append("${tabNumStr}[${k}][${v.toStr()}]\n")
            }
            return sb.toString()
        }

        fun LHMHelper.A3LHM<String, String, ArrayList<StateTransitionEventUnit>>.toStr(
            tabNum: Int = 0,
        ): String {
            if (tabNum < 0) throw IllegalArgumentException()
            val tabNumStr = "\t".repeat(tabNum)
            val tabNumStr1 = "\t".repeat(tabNum + 1)
            val tabNumStr2 = "\t".repeat(tabNum + 2)
            val sb = StringBuilder()
            this.touch { event: String, ttid: String, steu: ArrayList<StateTransitionEventUnit> ->
                steu.map {
                    sb.append("${tabNumStr}${event}\n")
                    sb.append("${tabNumStr1}${ttid}\n")
                    sb.append("${tabNumStr2}${it}\n")
                }
            }
            return sb.toString()
        }

        object TCLHMExpand {
            fun LinkedHashMap<Transition, ClockConstraint?>.toStr(
                tabNum: Int = 0,
            ): String {
                if (tabNum < 0) throw IllegalArgumentException()
                val tabNumStr = "\t".repeat(tabNum)
                val tabNumStr1 = "\t".repeat(tabNum + 1)
                val sb = StringBuilder()
                this.map { (k: Transition, v: ClockConstraint?) ->
                    sb.append("${tabNumStr}${k.toStr()}\n")
                    sb.append("${tabNumStr1}${v}\n")
                }
                return sb.toString()
            }
        }

        fun SCXMLTuple.toStr(
            tabNum: Int = 0,
        ): String {
            val ifPrintDataSCXML = false
            val ifPrintTransitionCondLHM = false
            val tabNum1 = tabNum + 1
            val tabNum2 = tabNum + 2
            val tabNumStr = "\t".repeat(tabNum)
            val tabNumStr1 = "\t".repeat(tabNum1)
            val sb = StringBuilder()
            sb.append("${tabNumStr}scxmlTuple:\n")
            if (ifPrintDataSCXML) {
                sb.append(this.dataSCXML.toStr(tabNum1))
            }
            sb.append("${tabNumStr1}eventUnitLHM:\n")
            sb.append(eventUnitLHM.toStr(tabNum2))
            if (ifPrintTransitionCondLHM) {
                sb.append("${tabNumStr1}transitionCondLHM:\n")
                sb.append(transitionCondLHM.toStr(tabNum2))
            }
            return sb.toString()
        }
    }
}