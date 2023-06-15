package helper.scxml.scxml2

import helper.base.ConstraintHelper
import helper.base.DebugHelper.Debugger
import helper.base.DebugHelper.Debugger.Companion.getDebuggerByInt
import helper.base.DebugHelper.DebuggerList
import helper.base.DebugHelper.DebuggerList.Companion.getDebuggerList
import helper.base.LHMHelper.A3LHM
import helper.base.LHMHelper.LHMExpand.add
import helper.base.MathHelper
import helper.base.PrintHelper.StringTo.toPrintln
import helper.block.BlockHelper.Expand.BlockTo.toBracketBlock1
import helper.block.BlockHelper.Expand.ToBlock.toBlock
import helper.block.ScxmlBlockHelper
import helper.block.ScxmlBlockHelper.ScxmlBlockFactory.BlockState
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.ScxmlVarHelper.ClockConstraint.ToClockConstraint.toClockConstraint
import helper.scxml.scxml1.Scxml1Helper.LHMExpand.addState
import helper.scxml.scxml2.Expand.EnterableStateExpand.toStateTreeNode
import helper.scxml.scxml2.Expand.ExecutableExpand.doExecutable
import helper.scxml.scxml2.Expand.LHMExpand.addState
import helper.scxml.scxml2.Expand.LHMExpand.toStringDoubleLHM
import helper.scxml.scxml2.Expand.SCXMLExecutorExpand.isInState
import helper.scxml.scxml2.Expand.SCXMLExpand.getInitialState
import helper.scxml.scxml2.Expand.SCXMLExpand.touchTransition
import helper.scxml.scxml2.Expand.SCXMLExpand.touchTransitionTarget
import helper.scxml.scxml2.Expand.ToBlockClass.toBlockDataModel
import helper.scxml.scxml2.Expand.ToBlockClass.toBlockState
import helper.scxml.scxml2.Expand.TransitionExpand.toStr
import helper.scxml.scxml2.Expand.TransitionTargetExpand.calculateExpectation
import helper.scxml.scxml2.Expand.TransitionTargetExpand.doEntriesAssign
import helper.scxml.scxml2.Expand.TransitionTargetExpand.doExitsAssign
import helper.scxml.scxml2.Expand.TransitionTargetExpand.touchFromRootToThis
import helper.scxml.scxml2.Expand.toSignalEvent
import helper.scxml.scxml2.Scxml2Helper.StateTransitionEventUnit
import helper.scxml.strategy.ScxmlOneStrategyHelper
import org.apache.commons.scxml2.Evaluator
import org.apache.commons.scxml2.SCXMLExecutor
import org.apache.commons.scxml2.model.*
import kotlin.math.max
import kotlin.math.min

class SCXMLTuple(
    //数据模型解析器
    val evaluator: Evaluator,
    //引擎
    val executor: SCXMLExecutor,
    //SCXML实例
    scxml: SCXML,
) {
    val initialStateList = ArrayList<String>()
    val finalStateList = ArrayList<String>()
    val renStateList = ArrayList<String>()

    val stateNeedConsiderClockListLHM = LinkedHashMap<String, ArrayList<String>>()
    val stateDataIncrementLHM = A3LHM<String, String, Double>()

    val dataSCXML = DataSCXML(scxml)

    fun reset() {
        executor.reset()
        dataSCXML.reset()
    }

    val idTransitionTargetLHM = LinkedHashMap<String, TransitionTarget>().also {
        dataSCXML.scxml.touchTransitionTarget { _, tt ->
            it.addState(tt)
        }
    }

    val idBlockStateLHM = LinkedHashMap<String, BlockState>().also {
        val rootState = BlockState(id = "0")
        it.addState(rootState)
        this.dataSCXML.scxml.touchTransitionTarget { tf, tt ->
            val tfState = if (tf == null) {
                rootState
            } else {
                it[tf.id]!!
            }
            val ttsState = tt.toBlockState()
            it.addState(ttsState)
            tfState.children.add(ttsState)
            when (tt) {
                is Initial -> {
                    tfState.initialBlockState = ttsState
                }
                is Final -> {
                    tfState.finalBlockStates.add(ttsState)
                }
                else -> {
                }
            }
        }
    }

    //event,transitionTarget起点,eventUnit
    val eventUnitLHM = A3LHM<String, String, ArrayList<StateTransitionEventUnit>>().also {
        val touchedTransitionSet = LinkedHashSet<Transition>()
        dataSCXML.scxml.touchTransition { tt, t ->
            if (!touchedTransitionSet.contains(t)) {
                touchedTransitionSet.add(t)
                val ste = StateTransitionEventUnit(
                    stateId = tt.id,
                    transition = t,
                    cond = t.cond.toClockConstraint(),
                    event = t.event,
                )
                it.add(ste.event, tt.id, ArrayList())
                it[ste.event]!![tt.id]!!.add(ste)
            }
        }
    }

    val transitionCondLHM = LinkedHashMap<Transition, ClockConstraint?>().also {
        this.dataSCXML.scxml.touchTransition { _, t ->
            val cond: String? = t.cond
            it[t] = cond.toClockConstraint()
        }
    }

    fun touchEventUnitLHM(init: (StateTransitionEventUnit) -> Unit = {}) {
        eventUnitLHM.touch { _, _, arrayList ->
            arrayList.map {
                init(it)
            }
        }
    }

    val rootStateTreeNode = Scxml2Helper.StateTreeNode.getRootStateTreeNode().also {
        val lhm = LinkedHashMap<TransitionTarget?, Scxml2Helper.StateTreeNode>()
        lhm[null] = it
        this.dataSCXML.scxml.touchTransitionTarget { tf: TransitionTarget?, tt: TransitionTarget ->
            lhm[tt] = lhm[tf]!!.addChildAndReturnChild(tt)
        }
    }

    init {
        this.touchEventUnitLHM {
            it.transition.cond = null
        }
    }

    val activeStates: List<EnterableState>
        get() {
            return this.executor.status.activeStates.filterNotNull()
        }

    val activeStateIds: List<String>
        get() {
            return this.activeStates.map { it.id }
        }

    val activeStatesString: String
        get() {
            if (activeStates.size != 1) throw IllegalArgumentException()
            return activeStates.joinToString { it.id }
        }

    fun getStatusString(
        ifAppendRawActiveStates: Boolean = false,
    ): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(
            this.activeStates.toStateTreeNode(
                rootStateTreeNode
            )
        )
        if (ifAppendRawActiveStates) {
            stringBuilder.append(" , ")
            stringBuilder.append(
                this.executor.status.activeStates.filterNotNull().joinToString {
                    it.id
                }.toBlock().toBracketBlock1().getStr()
            )
        }
        stringBuilder.append(" , ")
        stringBuilder.append(
            this.executor.stateMachine.datamodel.data.joinToString {
                "${it.id}=${it.expr}"
            }.toBlock().toBracketBlock1().getStr()
        )
        return stringBuilder.toString()
    }

    fun statusPrintln(
        ifAppendRawActiveStates: Boolean = false,
    ) {
        getStatusString(ifAppendRawActiveStates).toPrintln()
    }

    fun getCurrentConfigureStr(): String {
        val myStates = ArrayList<BlockState>()
        this.executor.status.activeStates.filterNotNull().map {
            it.touchFromRootToThis {
                myStates.add(this.idBlockStateLHM[it.id]!!)
            }
        }
        return ScxmlBlockHelper.ScxmlBlockFactory.Configuration(
            myStates,
            this.dataSCXML.scxml.datamodel.toBlockDataModel()
        ).toFormalBlock().getStr()
    }

    fun doExecutable(
        event: String,
        doOnEntry: (TransitionTarget) -> Unit = {},
        debuggerList: DebuggerList = getDebuggerList(0),
    ) {
        debuggerList.startPln("doExecutable")
        debuggerList.pln("event=$event")
        activeStates.map { state: EnterableState ->
            debuggerList.pln("state=${state.id}")
            if (state is TransitionalState) {
                state.transitionsList.map { transition: Transition ->
                    //注意还有events
                    debuggerList.pln("transition.event=${transition.event}")
                    debuggerList.pln("event=${event}")
                    if (transition.event == event) {
                        //不考虑cond，因为在init中置为null了
                        state.doExitsAssign(dataSCXML, debuggerList)
                        transition.doExecutable(dataSCXML, debuggerList)
                        transition.targets.map {
                            it.doEntriesAssign(dataSCXML, debuggerList)
                            doOnEntry(it)
                        }
                    }
                }
            }
        }
        debuggerList.endPln()
    }

    fun fireEvent(
        event: String,
        doOnEntryFun: (TransitionTarget) -> Unit = {},
        countClockValueFun: (SCXMLTuple, String) -> Unit = { _, _ -> },
        debuggerList: DebuggerList = getDebuggerList(0),
    ) {
        debuggerList.pln(
            getStatusString(),
            arrayListOf(0, 1),
        )
        debuggerList.pln(
            "fireEvent:${event}",
            arrayListOf(0, 1),
        )
        countClockValueFun(this, event)
        this.doExecutable(event, doOnEntryFun, debuggerList)
        this.executor.triggerEvent(event.toSignalEvent())
        debuggerList.pln(
            getStatusString(),
            arrayListOf(0, 1),
        )
    }

    fun tryFire(envStrategyNode: ScxmlOneStrategyHelper.SNode) {
        ScxmlOneStrategyHelper.getStrategyLeafNode(
            envStrategyNode = envStrategyNode,
            IDataExpand = this.dataSCXML,
            filterStateFun = { it ->
                var b = true
                it.map {
                    if (!b) return@map
                    if (!this.executor.isInState(it)) {
                        b = false
                    }
                }
                b
            },
        )?.let { sNode: ScxmlOneStrategyHelper.SNode ->
            var leftTime = Int.MAX_VALUE
            sNode.getTrace().map {
                if (it.isMiddleNode()) {
                    if (it.strategyNodeKeyType!! is ScxmlOneStrategyHelper.StrategyNodeKeyTypeVarConstraint) {
                        it.strategyNodeKeyType as ScxmlOneStrategyHelper.StrategyNodeKeyTypeVarConstraint
                        it.strategyNodeKeyType.key.toClockConstraint()!!.let {
                            var minTime = it.minV
                            val maxTime = it.maxV
                            val nowTime = this.dataSCXML.getData(it.varId)!!.expr.toInt()
                            minTime = max(minTime, nowTime)
                            leftTime = min(leftTime, maxTime - minTime + 1)
                        }
                    }
                }
            }
            val event = MathHelper.getRandomStringWithLeftTime(
                sNode.eventDPLHM!!,
                leftTime,
            ) ?: return@let
            this.fireEvent(event)
        }
    }

    // 直接计算数学期望
    // 有大问题
    // 可弃
    fun calculateExpectation(
        envStrategyNode: ScxmlOneStrategyHelper.SNode,
        globalTimeMax: Int,
    ): Double {
        return this.dataSCXML.scxml.getInitialState().calculateExpectation(
            this,
            envStrategyNode,
            LinkedHashMap<String, Data>().also { lhm ->
                arrayListOf("T", Res.globalTimeId).map {
                    lhm[it] = this.dataSCXML.getData(it)!!
                }
            },
            globalTimeMax,
        )
    }

    fun calculateCostConsiderInOut(
        //资源在这，统计结果也在这
        myDataList: Scxml2Helper.MyDataList,
        activeActiveStateTreeNode: Scxml2Helper.ActiveStateTreeNode = Scxml2Helper.ActiveStateTreeNode.getRootStateTreeNode(),
        debugger: Debugger = getDebuggerByInt(1, 0),
    ) {
        debugger.startPln("calculateCostConsiderInOut")
        debugger.pln("考虑进出状态")
        debugger.pln("activeStateTreeNode=${activeActiveStateTreeNode}")
        debugger.startPln("childrenFirstTouch")
        var ifEnterOrOutState = false
        activeActiveStateTreeNode.childrenFirstTouch { stateTreeNode ->
            debugger.startPln("StateTreeNode")
            if (stateTreeNode.transitionTargetStatus == Scxml2Helper.TransitionTargetStatus.entered) {
                val tt = stateTreeNode.tt
                if (tt == null) {
                    //scxml
                    this.dataSCXML.scxml.children.map {
                        stateTreeNode.addChildAndDo(it, myDataList)
                        ifEnterOrOutState = true
                    }
                } else {
                    when (tt) {
                        is Final -> {
                            stateTreeNode.tf!!.removeChildAndDo(stateTreeNode, myDataList)
                            ifEnterOrOutState = true
                        }
                        is State -> {
                            tt.first?.let {
                                stateTreeNode.addChildAndDo(idTransitionTargetLHM[it]!!, myDataList)
                                ifEnterOrOutState = true
                            }
                        }
                        is Parallel -> {
                            tt.children.map {
                                stateTreeNode.addChildAndDo(it, myDataList)
                                ifEnterOrOutState = true
                            }
                        }
                    }
                }
                debugger.pln("activeStateTreeNode=$activeActiveStateTreeNode")
            }
            debugger.endPln()
            return@childrenFirstTouch false
        }
        debugger.endPln()
        debugger.endPln()
        if (ifEnterOrOutState) {
            calculateCostConsiderInOut(
                myDataList,
                activeActiveStateTreeNode,
                debugger,
            )
        }
    }

    // 搁置
    fun calculateCostConsiderStrategy(
        envStrategyNode: ScxmlOneStrategyHelper.SNode,
        //资源在这，统计结果也在这
        myDataList: Scxml2Helper.MyDataList,
        clockIndexes: List<String>,
        //用资源来约束
        stopConstraint: ConstraintHelper.CompositeConstraint,
        rootActiveStateTreeNode: Scxml2Helper.ActiveStateTreeNode = Scxml2Helper.ActiveStateTreeNode.getRootStateTreeNode(),
        debugger: Debugger = getDebuggerByInt(1, 0),
    ) {
        debugger.startPln("calculateCostConsiderStrategy")
        debugger.pln("考虑变迁")

        ScxmlOneStrategyHelper.getStrategyLeafNode(
            envStrategyNode = envStrategyNode,
            IDataExpand = myDataList,
            filterStateFun = {
                var boolean = true
                it.map {
                    if (boolean) {
                        if (!rootActiveStateTreeNode.contains(it)) {
                            boolean = false
                        }
                    }
                }
                boolean
            }
        )?.let { sNode ->
            //多自动机协同运行，不止会获得一个策略叶结点
            sNode.eventDPLHM!!.let { eDPLHM ->
                val eDPLHMSum = eDPLHM.values.sum()
                eDPLHM.map { (event, p) ->
                    eventUnitLHM[event]?.map { (stateId, value) ->
                        value.map { stateTransitionEventUnit ->
                            //transition的所有targets都要考虑
                            //需要复制激活状态集合、数据取值、事件队列等，才能进入下一层计算
                        }
                    }
                }
            }
        }


    }

    // 直接计算数学期望
    // 有大问题
    // 可弃
    fun calculateCost(
        envStrategyNode: ScxmlOneStrategyHelper.SNode,
        //资源在这，统计结果也在这
        myDataList: Scxml2Helper.MyDataList,
        clockIndexes: List<String>,
        //用资源来约束
        stopConstraint: ConstraintHelper.CompositeConstraint,
        rootActiveStateTreeNode: Scxml2Helper.ActiveStateTreeNode = Scxml2Helper.ActiveStateTreeNode.getRootStateTreeNode(),
        debugger: Debugger = getDebuggerByInt(1, 0, 11),
        diGuiNum: Int = 0,
    ) {
        if (diGuiNum == 10) {
            return
        }
        debugger.startPln("calculateCost")
        // 全局时间
        val gTimeData = myDataList.dataLHM[Res.globalTimeId]!!
        val gTime = gTimeData.expr.toInt()
        val ifRootStateTreeNodeToEnd = rootActiveStateTreeNode.ifRootStateTreeNodeToEnd()
        val stopConstraintMeet = stopConstraint.meet(myDataList.dataLHM.toStringDoubleLHM())
        debugger.pln("ifRootStateTreeNodeToEnd=${ifRootStateTreeNodeToEnd}")
        debugger.pln("stopConstraintMeet=${stopConstraintMeet}")
        val ifStop = ifRootStateTreeNodeToEnd || stopConstraintMeet
        if (ifStop) {
            debugger.pln("activeStateTreeNode=$rootActiveStateTreeNode")
            return
        }

        if (rootActiveStateTreeNode.ifRootStateTreeNodeToStart()) {
            calculateCostConsiderInOut(
                myDataList,
                rootActiveStateTreeNode,
                debugger,
            )
        }

        debugger.pln("考虑时间")
        debugger.pln("收集 clockConstraint")
        val clockConstraints = ArrayList<ClockConstraint>()
        debugger.pln("activeStateTreeNode=${rootActiveStateTreeNode}")
        debugger.startPln("childrenFirstTouch")
        rootActiveStateTreeNode.childrenFirstTouch { activeStateTreeNode: Scxml2Helper.ActiveStateTreeNode ->
            debugger.startPln("ActiveStateTreeNode")
            debugger.pln("activeStateTreeNode=${activeStateTreeNode}")
            val tt = activeStateTreeNode.tt
            debugger.pln("transitionTarget=${tt?.id}")
            tt?.let { transitionTarget: TransitionTarget ->
                if (transitionTarget is TransitionalState) {
                    debugger.pln("transitionTarget is TransitionalState")
                    transitionTarget.transitionsList.map { transition: Transition ->
                        debugger.pln("transition=${transition.toStr()}")
                        this.transitionCondLHM[transition]?.let { clockConstraint ->
                            clockConstraints.add(clockConstraint)
                        }
                    }
                }
            }
            debugger.endPln()
            true
        }
        debugger.endPln()
        debugger.pln("收集 clockConstraint 之后")

        val (
            minCCMinMinusATime,
            minATime,
        ) = if (clockConstraints.size == 0) {
            Pair(0, 0)
        } else {
            var minCCMinMinusATime = Int.MAX_VALUE
            var minATime = Int.MAX_VALUE
            clockConstraints.map { clockConstraint: ClockConstraint ->
                debugger.pln("clockConstraint=${clockConstraint}")
                val aTimeData: Data = myDataList.dataLHM[clockConstraint.varId]!!
                val aTime = aTimeData.expr.toInt()
                val nowMinus = clockConstraint.minV - aTime
                if (nowMinus < minCCMinMinusATime) {
                    minCCMinMinusATime = nowMinus
                    minATime = aTime
                }
            }
            debugger.pln("minATime=${minATime}")
            debugger.pln("minCCMinMinusATime=${minCCMinMinusATime}")
            val aTime1 = minATime + minCCMinMinusATime
            debugger.pln("aTime1=${aTime1}")
            Pair(minCCMinMinusATime, minCCMinMinusATime)
            //celue
        }

        //celue
        if (minCCMinMinusATime == 0) {
            calculateCostConsiderStrategy(
                envStrategyNode,
                myDataList,
                clockIndexes,
                stopConstraint,
                rootActiveStateTreeNode,
                debugger,
            )
        }
    }

    fun toData(
        lhm: LinkedHashMap<String, String> = LinkedHashMap(),
    ): LinkedHashMap<String, String> {
        this.dataSCXML.scxml.datamodel.data.map {
            lhm.add(it.id, it.expr)
        }
        return lhm
    }
}

