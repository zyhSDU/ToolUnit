package helper.scxml.strategy

import helper.base.DebugHelper.Debugger
import helper.base.DebugHelper.Debugger.Companion.getDebuggerByInt
import helper.base.LHMHelper.LHMExpand.toLinkedHashMap
import helper.block.BlockHelper
import helper.block.BlockHelper.Expand.BlockTo.toLineBlock
import helper.block.BlockHelper.Expand.LHMExpand.toBlock
import helper.block.BlockHelper.Expand.ToBlock.toBlock
import helper.scxml.IDataExpandHelper
import helper.scxml.IDataExpandHelper.Expand.ifMeet2
import helper.scxml.ScxmlVarHelper.ClockConstraint.ToClockConstraint.toClockConstraint
import helper.scxml.scxml1.Scxml1Helper

object ScxmlMultiStrategyHelper {
    //StrategyNodeKeyType
    interface SNodeKeyType {
        fun toBlock(): BlockHelper.Block

        fun toBlockString(): String {
            return this.toBlock().toString()
        }
    }

    data class SNodeKeyTypeState(
        val stateId: String
    ) : SNodeKeyType {
        override fun toBlock(): BlockHelper.Block {
            return this.stateId.toBlock()
        }
    }

    open class SNodeKeyTypeVarConstraint(
        open val key: String,
    ) : SNodeKeyType {
        override fun toBlock(): BlockHelper.Block {
            return this.key.toBlock()
        }

        override fun toString(): String {
            return "SNodeKeyTypeVarConstraint(key='$key')"
        }
    }

    class SNodeKeyTypeClockConstraint(
        override val key: String,
    ) : SNodeKeyTypeVarConstraint(
        key
    ) {
        override fun toString(): String {
            return "SNodeKeyTypeClockConstraint(key='$key')"
        }
    }

    class SNodeType(
        val name: String
    )

    val sNodeTypeRoot = SNodeType("root")
    val sNodeTypeMiddle = SNodeType("middle")
    val sNodeTypeLeaf = SNodeType("leaf")

    /**
     * strategy node
     * 策略结点
     */
    class SNode(
        //root，中，叶
        //空，有，有//判root
        var father: SNode? = null,
        //空，有，空//判中
        val sNodeKeyType: SNodeKeyType? = null,
        //有，有，空//判叶
        val children: ArrayList<SNode>? = null,
        //空，空，有//判叶
        val eventDPLHM: LinkedHashMap<String, Double>? = null,
    ) {
        init {
            father?.addChild(this)
        }

        fun touch(
            init: (SNode) -> Boolean,
        ) {
            if (init(this)) {
                this.children?.map {
                    it.touch(init)
                }
            }
        }

        fun touchFromRootToMe(
            init: (SNode) -> Unit = {},
        ) {
            if (this.father != null) {
                this.father!!.touchFromRootToMe(init)
            }
            init(this)
        }

        fun getTrace(
            arrayList: ArrayList<SNode> = ArrayList(),
        ): ArrayList<SNode> {
            this.touchFromRootToMe {
                arrayList.add(it)
            }
            return arrayList
        }

        fun findToLastStateNode(): SNode? {
            return when {
                isRootNode() -> {
                    null
                }
                this.sNodeKeyType is SNodeKeyTypeState -> {
                    this
                }
                else -> {
                    father!!.findToLastStateNode()
                }
            }
        }

        fun removeChild(sNode: SNode) {
            this.children!!.remove(sNode)
            sNode.father = null
        }

        fun addChild(vararg sNodes: SNode) {
            sNodes.map { sNode ->
                this.children!!.add(sNode)
                sNode.father = this
            }
        }

        fun isRootNode(): Boolean {
            return father == null
        }

        fun isMiddleNode(): Boolean {
            return sNodeKeyType != null
        }

        fun isLeafNode(): Boolean {
            return children == null
        }

        fun getNodeType(): String {
            val sNodeType: SNodeType = when {
                isRootNode() -> {
                    sNodeTypeRoot
                }
                isMiddleNode() -> {
                    sNodeTypeMiddle
                }
                else -> {
                    assert(isLeafNode())
                    sNodeTypeLeaf
                }
            }
            return sNodeType.name
        }

        fun toBlock(): BlockHelper.Block {
            val lhm = LinkedHashMap<SNode, BlockHelper.Block>()
            this.touch { nowNode ->
                if (nowNode.isLeafNode()) {
                    nowNode.eventDPLHM!!.toBlock().toLineBlock().let { block ->
                        lhm[nowNode.father]!!.wBlocks[0].addBlock(block)
                    }
                } else {
                    val nowNameBlock: BlockHelper.Block = if (nowNode.isRootNode()) {
                        "root".toBlock()
                    } else {
                        nowNode.sNodeKeyType!!.toBlock()
                    }
                    val nowBlock = Scxml1Helper.bf.getBlock(
                        template = "\n${BlockHelper.b0}" +
                                "${BlockHelper.w0}${BlockHelper.b1}",
                        arrayListOf(
                            nowNameBlock,
                            Scxml1Helper.bf.getEmptyBlock(),
                        ),
                    )
                    lhm[nowNode] = nowBlock
                    if (!nowNode.isRootNode()) {
                        lhm[nowNode.father]!!.wBlocks[0].addBlock(nowBlock)
                    }
                }
                true
            }
            return lhm[this]!!
        }

        override fun toString(): String {
            if (this.isRootNode()) return "root"
            if (this.isMiddleNode()) {
                return this.sNodeKeyType!!.toBlockString()
            }
            assert(this.isLeafNode())
            return this.eventDPLHM!!.toBlock().getStr()
        }

        companion object {
            fun getRootNode(): SNode {
                return SNode(
                    children = ArrayList(),
                )
            }

            fun getMiddleNode(
                sNodeKeyType: SNodeKeyType,
            ): SNode {
                return SNode(
                    sNodeKeyType = sNodeKeyType,
                    children = ArrayList(),
                )
            }

            fun getMiddleStateNode(
                stateId: String
            ): SNode {
                return getMiddleNode(SNodeKeyTypeState(stateId))
            }

            fun getMiddleConstraintNode(
                constraint: String
            ): SNode {
                return getMiddleNode(SNodeKeyTypeVarConstraint(constraint))
            }

            fun getLeafNode(
                vararg stringDoublePair: Pair<String, Double>,
            ): SNode {
                return SNode(
                    eventDPLHM = stringDoublePair.toLinkedHashMap(),
                )
            }
        }
    }

    //找叶子结点
    fun getStrategyLeafNode(
        envStrategyNode: SNode,
        dataExpand: IDataExpandHelper.IDataExpand,
        //用于过滤状态结点
        filterStateFun: (String) -> Boolean,
        debugger: Debugger = getDebuggerByInt(0)
    ): SNode? {
        var esn: SNode? = null
        envStrategyNode.touch { node ->
            if (node.isRootNode()) {
                debugger.pln("isRootNode")
                //继续往下找结点
                return@touch true
            }
            if (node.isMiddleNode()) {
                debugger.pln("\tisMiddleNode,strategyNodeKeyType=${node.sNodeKeyType}")
                node.sNodeKeyType!!.let {
                    if (it is SNodeKeyTypeState) {
                        //符合条件才继续往下找结点，不符合就不继续往下找结点
                        //这里不对，以为策略结构变成了多层结点，而不再是单层结点
                        return@touch filterStateFun(it.stateId)
                    } else {
                        assert(it is SNodeKeyTypeVarConstraint)
                        if (it is SNodeKeyTypeClockConstraint) {
                            val clockConstraint = it.key.toClockConstraint()
                            debugger.pln("clockConstraint=${clockConstraint}")
                            return@touch clockConstraint!!.ifMeet2(dataExpand)
                        }
                    }
                }
            }
            if (node.isLeafNode()) {
                debugger.pln("\t\tisLeafNode")
                esn = node
                //不再继续往下找结点
                return@touch false
            }
            //找不到
            //不再继续往下找结点
            return@touch false
        }
        //找到叶子结点
        debugger.pln("esn=${esn}")
        return esn
    }
}