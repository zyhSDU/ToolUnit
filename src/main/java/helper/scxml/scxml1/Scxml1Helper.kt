package helper.scxml.scxml1

import helper.base.MathHelper
import helper.base.PrintHelper.StringTo.toPrintln
import helper.base.ResourceHelper
import helper.base.ScannerHelper
import helper.block.BlockHelper
import helper.block.BlockHelper.Expand.BlockListTo.joinToAssignBracketBlock2
import helper.block.BlockHelper.Expand.BlockListTo.joinToPrefixBracketBlock1
import helper.block.BlockHelper.Expand.BlockTo.toPrefixBracketBlock1
import helper.block.BlockHelper.Expand.ToAssignBlock.toAssignBlock
import helper.block.BlockHelper.Expand.ToBlock.toBlock
import helper.block.BlockHelper.Expand.ToBlock.toLineBlock
import helper.block.ScxmlBlockHelper
import helper.scxml.IDataExpandHelper
import helper.scxml.scxml1.Scxml1Helper.DataExpand.exprAddOne
import helper.scxml.scxml1.Scxml1Helper.DataExpand.setExprZero
import helper.scxml.scxml1.Scxml1Helper.LHMExpand.addState
import helper.scxml.scxml1.Scxml1Helper.SCXMLExpand.toBlockState
import helper.scxml.scxml1.Scxml1Helper.SCXMLExpand.touchTransitionTarget
import helper.scxml.scxml1.Scxml1Helper.ToBlockClass.toBlockDataModel
import helper.scxml.scxml1.Scxml1Helper.ToBlockClass.toBlockState
import helper.scxml.scxml1.Scxml1Helper.ToSelfBlock.toSelfBlock
import helper.scxml.scxml1.Scxml1Helper.TransitionTargetExpand.touchFromRootToThis
import helper.scxml.scxml1.Scxml1Helper.TransitionTargetExpand.touchTransitionTarget
import helper.scxml.ScxmlVarHelper.ClockConstraint
import helper.scxml.ScxmlVarHelper.ClockConstraint.ToClockConstraint.toClockConstraint
import helper.scxml.strategy.ScxmlOneStrategyHelper.SNode
import helper.scxml.strategy.ScxmlOneStrategyHelper.StrategyNodeKeyTypeVarConstraint
import helper.scxml.strategy.ScxmlOneStrategyHelper.getStrategyLeafNode
import org.apache.commons.scxml.Status
import org.apache.commons.scxml.env.AbstractStateMachine
import org.apache.commons.scxml.model.*
import kotlin.math.max
import kotlin.math.min

object Scxml1Helper {
    val bf = ScxmlBlockHelper.ScxmlBlockFactory.bf

    object LHMExpand {
        fun LinkedHashMap<String, TransitionTarget>.addState(
            state: TransitionTarget,
        ) {
            this[state.id] = state
        }

        fun LinkedHashMap<String, ScxmlBlockHelper.ScxmlBlockFactory.BlockState>.addState(
            blockState: ScxmlBlockHelper.ScxmlBlockFactory.BlockState
        ) {
            this[blockState.id] = blockState
        }

        fun LinkedHashMap<SNode, BlockHelper.Block>.toString2(): String {
            val sb = StringBuilder()
            this.map { (k, v) ->
                sb.append("${k.getNodeType()} : ${v.getStr()}\n")
            }
            return sb.toString()
        }
    }

    object ToSelfBlock {
        fun Transition.toSelfBlock(
        ): BlockHelper.Block {
            return this.event.toBlock().toAssignBlock("event")
                .toPrefixBracketBlock1("Transition")
        }

        fun State.toSelfBlock(
        ): BlockHelper.Block {
            return arrayListOf(
                this.id.toAssignBlock("id"),
                transitionsList.filterNotNull().map {
                    it as Transition
                    it.toSelfBlock()
                }.joinToAssignBracketBlock2("transitionsList"),
            ).joinToPrefixBracketBlock1("State")
        }

        fun Status.toSelfBlock(
        ): BlockHelper.Block {
            return arrayListOf(
                this.states.filterNotNull().map {
                    it as State
                    it.toSelfBlock()
                }.joinToAssignBracketBlock2("states"),
                this.events.filterNotNull().map {
                    it as String
                    it.toBlock()
                }.joinToAssignBracketBlock2("events"),
            ).joinToPrefixBracketBlock1("Status")
        }
    }

    object ToBlockClass {
        fun Data.toBlockData(): ScxmlBlockHelper.ScxmlBlockFactory.Data {
            return ScxmlBlockHelper.ScxmlBlockFactory.Data(this.id, this.expr)
        }

        fun Datamodel.toBlockDataModel(): ScxmlBlockHelper.ScxmlBlockFactory.DataModel {
            val resultData = ScxmlBlockHelper.ScxmlBlockFactory.DataModel()
            this.data.filterNotNull().map {
                it as Data
                resultData.dataList.add(it.toBlockData())
            }
            return resultData
        }

        fun TransitionTarget.toBlockState(): ScxmlBlockHelper.ScxmlBlockFactory.BlockState {
            return ScxmlBlockHelper.ScxmlBlockFactory.BlockState(id = this.id)
        }
    }

    object DataExpand {
        fun Data.exprEqualsInt(
            int: Int,
        ): Boolean {
            return this.expr.equals("$int")
        }

        fun Data.getIntExpr(): Int {
            return this.expr.toInt()
        }

        fun Data.exprAddOne() {
            this.expr = "${this.getIntExpr() + 1}"
        }

        fun Data.setExprZero() {
            this.expr = "0"
        }
    }

    object TransitionTargetExpand {
        fun TransitionTarget.touchTransitionTarget(
            tf: TransitionTarget?,
            init: (TransitionTarget?, TransitionTarget) -> Unit,
        ) {
            init(tf, this)
            when (this) {
                is State -> {
                    this.children.map { (_, v) ->
                        if (v != null) {
                            v as TransitionTarget
                            v.touchTransitionTarget(this, init)
                        }
                    }
                }
                is Parallel -> {
                    this.children.map { v ->
                        if (v != null) {
                            v as TransitionTarget
                            v.touchTransitionTarget(this, init)
                        }
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
    }

    object SCXMLExpand {
        fun SCXML.touchTransitionTarget(
            init: (TransitionTarget?, TransitionTarget) -> Unit = { tf, tt -> },
        ) {
            this.children.map { (_, v) ->
                if (v != null) {
                    v as TransitionTarget
                    v.touchTransitionTarget(null, init)
                }
            }
        }

        fun SCXML.toBlockState(
            sm: StateMachine,
        ): ScxmlBlockHelper.ScxmlBlockFactory.BlockState {
            val lhm = sm.idStateLHM
            val rootState = sm.idStateLHM["0"]!!
            this.touchTransitionTarget { tf, tt ->
                val tfState = if (tf == null) {
                    rootState
                } else {
                    lhm[tf.id]!!
                }
                val ttsState = lhm[tt.id]!!
                //
                if (tf is State && tf.first != null) {
                    tfState.initialBlockState = lhm[tf.first]!!
                }
                //
                tt.transitionsList.map {
                    it as Transition
                    ttsState.transitions.add(
                        ScxmlBlockHelper.ScxmlBlockFactory.Transition(
                            it.targets[0].let {
                                it as TransitionTarget
                                lhm[it.id]!!
                            },
                            arrayListOf(
                                ScxmlBlockHelper.ScxmlBlockFactory.Event(it.event),
                            ),
                        ),
                    )
                }
            }
            this.initial!!.run {
                rootState.initialBlockState = lhm[this]
            }
            //datamodel
            this.datamodel?.data?.filterNotNull()?.map {
                it as Data
                rootState.dataModel.dataList.add(
                    ScxmlBlockHelper.ScxmlBlockFactory.Data(
                        it.id,
                        it.expr,
                    ),
                )
            }
            return rootState
        }
    }

    object GetTestObject {
        fun getTestTransition(): Transition {
            val transition = Transition()
            transition.event = "event1"
            return transition
        }

        fun getTestState(id: String = "s1"): State {
            val state = State()
            state.id = id
            val transition1 = Transition()
            transition1.event = "e1"
            val transition2 = Transition()
            transition2.event = "e2"
            state.transitionsList.add(transition1)
            state.transitionsList.add(transition2)
            return state
        }

        fun getTestStatus(): Status {
            val status = Status()
            val state1 = getTestState("s1")
            val state2 = getTestState("s2")
            status.states.add(state1)
            status.states.add(state2)
            return status
        }

        fun getTestData(index: Int = 0): Data {
            val data = Data()
            data.id = "d_k_${index}"
            data.expr = "d_v_${index}"
            return data
        }

        fun getTestDataModel(): Datamodel {
            val dataModel = Datamodel()
            dataModel.data.add(getTestData(0))
            dataModel.data.add(getTestData(1))
            return dataModel
        }
    }

    val globalTimeId = "globalTime"

    open class StateMachine(
        string: String,
    ) : AbstractStateMachine(
        ResourceHelper.getResource(
            string,
        ),
    ), IDataExpandHelper.IDataExpand {
        val globalTime: Data = Data().also {
            it.id = globalTimeId
            it.expr = "0"
            this.engine.stateMachine.datamodel.addData(it)
        }

        val idTransitionTargetLHM = LinkedHashMap<String, TransitionTarget>().also {
            this.touchTransitionTarget { tf, tt ->
                it.addState(tt)
            }
        }

        val idStateLHM = LinkedHashMap<String, ScxmlBlockHelper.ScxmlBlockFactory.BlockState>().also {
            val rootState = ScxmlBlockHelper.ScxmlBlockFactory.BlockState(id = "0")
            it.addState(rootState)
            this.touchTransitionTarget { tf, tt ->
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
        val eventUnitLHM = LinkedHashMap<String, LinkedHashMap<String, ArrayList<StateTransitionEventUnit>>>().also {
            val touchedTransitionSet = LinkedHashSet<Transition>()
            touchTransition { transitionTarget, transition ->
                if (!touchedTransitionSet.contains(transition)) {
                    touchedTransitionSet.add(transition)
                    StateTransitionEventUnit(
                        stateId = transitionTarget.id,
                        transition = transition,
                        cond = transition.cond,
                        event = transition.event,
                    ).let { ste ->
                        if (it[ste.event] == null) {
                            it[ste.event] = LinkedHashMap()
                        }
                        if (it[ste.event]!![transitionTarget.id] == null) {
                            it[ste.event]!![transitionTarget.id] = ArrayList()
                        }
                        it[ste.event]!![transitionTarget.id]!!.add(ste)
                    }
                }
            }
        }

        val transitionCondLHM = LinkedHashMap<Transition, String?>().also {
            touchTransition { tf, tt ->
                it[tt] = tt.cond
            }
        }

        init {
            //updateEventCond
            touchEventUnitLHM {
                // it.transition.cond.replace("&lt;","<")
                it.transition.cond = ""
            }
        }

        //相当于SCXML.touch,不应存在在这里
        fun touchTransitionTarget(
            init: (TransitionTarget?, TransitionTarget) -> Unit = { tf, tt -> },
        ) {
            this.engine.stateMachine.touchTransitionTarget(init)
        }

        fun touchTransition(
            init: (TransitionTarget, Transition) -> Unit,
        ) {
            touchTransitionTarget { _, tt ->
                tt.transitionsList.filterNotNull().map { t ->
                    t as Transition
                    init(tt, t)
                }
            }
        }

        fun touchEventUnitLHM(
            init: (StateTransitionEventUnit) -> Unit = {},
        ) {
            eventUnitLHM.map {
                it.value.map {
                    it.value.map {
                        init(it)
                    }
                }
            }
        }

        fun getEvents(): ArrayList<String> {
            val arr = ArrayList<String>()
            touchTransitionTarget { tf, it ->
                it.transitionsList.filterNotNull().map {
                    it as Transition
                    it.event?.run {
                        arr.add(this)
                    }
                }
            }
            return arr
        }

        fun toLatexBlock(): BlockHelper.Block {
            var block: BlockHelper.Block = bf.getEmptyBlock()
            this.engine.stateMachine?.run {
                val s0 = this.toBlockState(this@StateMachine)
                block = bf.getEmptyBlock(
                    ".".toLineBlock(),
                    bf.getNewLineBlock(),
                    s0.toLatexBlock(),
                    bf.getNewLineBlock(),
                )
            }
            return block
        }

        fun printlnLatexBlockString() {
            this.toLatexBlock().getStr().toPrintln()
        }

        //草率，没有结构化
        fun toStructBlock(): BlockHelper.Block {
            val mainBlock = bf.getEmptyBlock()
            fun BlockHelper.Block.toMainBlock() {
                mainBlock.addBlock(this)
            }
            touchTransitionTarget { _, tt ->
                "state:${tt.id}".toLineBlock().toMainBlock()
                tt.onExit.let { onExit: OnExit ->
                    "\tonexit:".toLineBlock().toMainBlock()
                    onExit.actions.map { action: Any? ->
                        if (action != null) {
                            action as Assign
                            "\t\tAssign(name=${action.name},expr=${action.expr})".toLineBlock().toMainBlock()
                        }
                    }
                }
                tt.transitionsList.filterNotNull().map {
                    it as Transition
                    it.event.let { event: String ->
                        "\tevent:${event}".toLineBlock().toMainBlock()
                    }
                    transitionCondLHM[it].let { cond: String? ->
                        "\t\tcond:${cond}".toLineBlock().toMainBlock()
                    }
                }
            }
            return mainBlock
        }

        fun printlnStructBlockString() {
            this.toStructBlock().getStr().toPrintln()
        }

        fun isOnState(stateId: String): Boolean {
            var res = false
            this.engine.currentStatus.states.filterNotNull().map {
                it as State
                if (it.id == stateId) {
                    res = true
                    return@map
                }
            }
            return res
        }

        override fun touchData(
            dataId: String, init: (Any) -> Unit,
        ) {
            this.engine.stateMachine.datamodel.data.filterNotNull().map {
                it as Data
                if (it.id == dataId) {
                    init(it)
                    return@map
                }
            }
        }

        override fun getData(
            dataId: String,
        ): Data? {
            var data: Data? = null
            touchData(dataId) {
                it as Data
                data = it
            }
            return data
        }

        override fun setDataExpr(
            dataId: String,
            dataExpr: String,
        ) {
            touchData(dataId) {
                it as Data
                it.expr = dataExpr
            }
        }

        fun getDataEqualById(
            id: String,
            equalValue: String,
        ): Boolean {
            var res = false
            touchData(id) {
                it as Data
                res = it.expr.toInt() == equalValue.toDouble().toInt()
            }
            return res
        }

        override fun setDataExprZero(dataId: String) {
            touchData(dataId) {
                it as Data
                it.setExprZero()
            }
        }

        override fun ifDataExprEqualString(dataId: String, dataExpr: String): Boolean {
            return getData(dataId)!!.expr.equals(dataExpr)
        }

        override fun setDataExprAddOne(dataId: String) {
            touchData(dataId) {
                it as Data
                it.exprAddOne()
            }
        }

        fun getCurrentStatusInfo(): String {
            return this.engine.currentStatus.toSelfBlock().getStr()
        }

        fun printCurrentStatusInfo() {
            getCurrentStatusInfo().toPrintln()
        }

        fun getCurrentConfigure(): ScxmlBlockHelper.ScxmlBlockFactory.Configuration {
            val myStates = ArrayList<ScxmlBlockHelper.ScxmlBlockFactory.BlockState>()
            this.engine.currentStatus.states.filterNotNull().map {
                it as State
                it
            }.map {
                it.touchFromRootToThis {
                    myStates.add(idStateLHM[it.id]!!)
                }
            }
            return ScxmlBlockHelper.ScxmlBlockFactory.Configuration(
                myStates,
                engine.stateMachine.datamodel.toBlockDataModel()
            )
        }

        fun printlnCurrentConfigure() {
            getCurrentConfigure().toFormalBlock().getStr().toPrintln()
        }

        // 不知当今是何结点，可弃
        fun tryFire(
            envStrategyNode: SNode,
        ) {
            getStrategyLeafNode(
                envStrategyNode = envStrategyNode,
                IDataExpand = this,
                filterStateFun = { it.size == 1 && this.isOnState(it.toList()[0]) },
            )?.let { sNode: SNode ->
                var leftTime = Int.MAX_VALUE
                sNode.getTrace().map {
                    if (it.isMiddleNode()) {
                        if (it.strategyNodeKeyType!! is StrategyNodeKeyTypeVarConstraint) {
                            it.strategyNodeKeyType as StrategyNodeKeyTypeVarConstraint
                            it.strategyNodeKeyType.key.toClockConstraint()!!.let {
                                var minTime = it.minV
                                val maxTime = it.maxV
                                val nowTime = this.getData(it.varId)!!.expr.toInt()
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
                println("fireEvent:${event}\t")
                this.fireEvent(event)
//                sNode.findToStateNode()!!.strategyNodeKeyType!!.let {
//                    this.idTransitionTargetLHM[it.key]!!.onExit.doAssign(this)
//                }
                this.printlnCurrentConfigure()
            }
        }

        //直接计算数学期望
        fun calculateExpectation(
            envStrategyNode: SNode,
            globalTimeMax: Int,
        ): Double {
            return this.engine.stateMachine.initialTarget.calculateExpectation(
                this,
                envStrategyNode,
                LinkedHashMap<String, Data>().also { lhm ->
                    arrayListOf("T", globalTimeId).map {
                        lhm[it] = this.getData(it)!!
                    }
                },
                globalTimeMax,
            )
        }

        fun testRun() {
            val stateMachine = this
            stateMachine.printCurrentStatusInfo()
            ScannerHelper.mapTrim {
                it.run {
                    stateMachine.fireEvent(this)
                    stateMachine.printCurrentStatusInfo()
                }
            }
        }

        //允许控制台输入事件，用于调试
        fun testRun2(
            es: ArrayList<ScxmlBlockHelper.ScxmlBlockFactory.Event> = arrayListOf(),
            cs: ArrayList<ScxmlBlockHelper.ScxmlBlockFactory.Configuration> = arrayListOf(),
            ifDone: () -> Unit = {},
        ) {
            val stateMachine = this
            stateMachine.printCurrentStatusInfo()
            ScannerHelper.mapTrim {
                it.run {
                    if (this == "done") {
                        ifDone()
                        return@mapTrim
                    }
                    stateMachine.fireEvent(this)
                    stateMachine.printCurrentStatusInfo()
                    es.add(ScxmlBlockHelper.ScxmlBlockFactory.Event(this))
                    cs.add(stateMachine.getCurrentConfigure())
                }
            }
        }
    }

    fun TransitionTarget.calculateExpectation(
        stateMachine: StateMachine,
        envStrategyNode: SNode,
        clocks: LinkedHashMap<String, Data>,
        globalTimeMax: Int,
    ): Double {
        val ifDebug = true
        if (ifDebug) "${this.id}.calculateExpectation".toPrintln()
        // 全局时间
        val gTimeData = clocks[globalTimeId]!!
        val gTime = gTimeData.expr.toInt()
        // 可能不只需要返回时间，其他参数值也可能要
        if (this is Final) return gTime.toDouble()
        var res = 0.0
        (this.transitionsList.size > 0).let {
            if (it) {
                // 只取第一个转换？
                this.transitionsList[0]
            } else {
                null
            }
        }?.let {
            if (ifDebug) stateMachine.transitionCondLHM[it]!!.toString().toPrintln()
            stateMachine.transitionCondLHM[it]?.toClockConstraint()
        }?.let { it: ClockConstraint ->
            if (ifDebug) it.toString().toPrintln()
            val aTimeData = clocks[it.varId]!!
            val aTime = aTimeData.expr.toInt()
            val aTime1 = max(aTime, it.minV)
            val aTime2 = min(globalTimeMax - gTime + aTime, it.maxV)
            val pCount = aTime2 - aTime1 + 1
            if (ifDebug) "${gTime},${aTime},${aTime1},${aTime2},pCount=${pCount}".toPrintln()
            if (pCount < 0) {
                return res
            }
            val resArr = Array(pCount) { 0.0 }
            (aTime1..aTime2).map { aTimeNew ->
                aTimeData.expr = aTimeNew.toString()
                gTimeData.expr = (aTimeNew - aTime + gTime).toString()
//                "gTime=${gTimeData.expr},aTime=${aTimeData.expr}".toPrintln()
                getStrategyLeafNode(
                    envStrategyNode = envStrategyNode,
                    IDataExpand = stateMachine,
                    filterStateFun = { it.size == 0 && it.toList()[0] == this.id },
                )?.let { sNode ->
//                    "sNode=${sNode}".toPrintln()
                    sNode.eventDPLHM!!.let { eDPLHM ->
                        val eDPLHMSum = eDPLHM.values.sum()
                        eDPLHM.map { (event, p) ->
//                            "event=${event},nodeId=${this.id},p=${p}".toPrintln()
                            //1个event可能对应多个transition，暂时先取第0个，1个transition可能对应多个target，暂时取第0个
                            stateMachine.eventUnitLHM[event]!![this.id]!![0].transition.targets[0].let { t ->
                                t as TransitionTarget
//                                this.onExit.doAssign(stateMachine)
                                resArr[aTimeNew - aTime1] += (p / eDPLHMSum) * t.calculateExpectation(
                                    stateMachine,
                                    envStrategyNode,
                                    clocks,
                                    globalTimeMax,
                                )
//                                "${this.id}.resArr=[${resArr.joinTo(StringBuilder())}]".toPrintln()
                            }
                        }
                    }
                }
            }
            res = resArr.sum() / pCount
            aTimeData.expr = aTime.toString()
            gTimeData.expr = gTime.toString()
        }
//        "state=${this.id},gTime=${gTime},res=${res}".toPrintln()
        return res
    }

    data class StateTransitionEventUnit(
        val stateId: String,
        val transition: Transition,
        val event: String,
        val cond: String?,
    )

//    fun Executable.doAssign(
//        stateMachine: StateMachine
//    ) {
//        this.actions.filterNotNull().map {
//            if (it is Assign) {
//                stateMachine.getDataById(it.name)?.expr = it.expr
//            }
//        }
//    }
}
