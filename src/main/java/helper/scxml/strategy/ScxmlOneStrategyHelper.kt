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

object ScxmlOneStrategyHelper {
    //StrategyNodeKeyType
    interface StrategyNodeKeyType {
        fun toBlock(): BlockHelper.Block

        fun toBlockString(): String {
            return this.toBlock().toString()
        }
    }

    data class StrategyNodeKeyTypeState(
        val stateIds: HashSet<String>
    ) : StrategyNodeKeyType {
        override fun toBlock(): BlockHelper.Block {
            return this.stateIds.toBlock()
        }
    }

    open class StrategyNodeKeyTypeVarConstraint(
        open val key: String,
    ) : StrategyNodeKeyType {
        override fun toBlock(): BlockHelper.Block {
            return this.key.toBlock()
        }

        override fun toString(): String {
            return "StrategyNodeKeyTypeVarConstraint(key='$key')"
        }
    }

    class StrategyNodeKeyTypeClockConstraint(
        override val key: String,
    ) : StrategyNodeKeyTypeVarConstraint(
        key
    ) {
        override fun toString(): String {
            return "StrategyNodeKeyTypeClockConstraint(key='$key')"
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
        val strategyNodeKeyType: StrategyNodeKeyType? = null,
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

        fun findToStateNode(): SNode? {
            return when {
                isRootNode() -> {
                    null
                }
                father!!.isRootNode() -> {
                    this
                }
                else -> {
                    father!!.findToStateNode()
                }
            }
        }

        fun isChildrenNull(): Boolean {
            return children == null
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
            return strategyNodeKeyType != null
        }

        fun isLeafNode(): Boolean {
            return isChildrenNull()
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

        /**
         * addMiddleNode
         * 任意个constraint
         */
        fun addMiddleNode(
            stateIds: HashSet<String>,
            constraints: ArrayList<String>? = null,
            vararg stringDoublePairs: Pair<String, Double>,
        ) {
            var nowNode = this
            if (stateIds.size > 0) {
                assert(this.isRootNode())
                val mn = getMiddleNode(StrategyNodeKeyTypeState(stateIds))
                nowNode.addChild(mn)
                nowNode = mn
            }
            constraints?.map {
                val strategyNodeKeyType = if (it.contains(".")) {
                    StrategyNodeKeyTypeVarConstraint(it)
                } else {
                    StrategyNodeKeyTypeClockConstraint(it)
                }
                val mn = getMiddleNode(strategyNodeKeyType)
                nowNode.addChild(mn)
                nowNode = mn
            }
            if (stringDoublePairs.isNotEmpty()) {
                val mn = getLeafNode(*stringDoublePairs)
                nowNode.addChild(mn)
                nowNode = mn
            }
        }

        /**
         * 0个constraint
         */
        fun addMiddleNode0(
            stateIds: HashSet<String>,
            vararg stringDoublePairs: Pair<String, Double>,
        ) {
            this.addMiddleNode(stateIds, null, *stringDoublePairs)
        }

        fun addMiddleNode0(
            stateId: String,
            vararg stringDoublePairs: Pair<String, Double>,
        ) {
            this.addMiddleNode0(hashSetOf(stateId), *stringDoublePairs)
        }

        /**
         * 1个constraint
         */
        fun addMiddleNode1(
            stateIds: HashSet<String>,
            constraint: String,
            vararg stringDoublePairs: Pair<String, Double>,
        ) {
            addMiddleNode(stateIds, arrayListOf(constraint), *stringDoublePairs)
        }

        fun addMiddleNode1(
            stateId: String,
            constraint: String,
            vararg stringDoublePairs: Pair<String, Double>,
        ) {
            addMiddleNode1(hashSetOf(stateId), constraint, *stringDoublePairs)
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
                        nowNode.strategyNodeKeyType!!.toBlock()
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
                return this.strategyNodeKeyType!!.toBlockString()
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
                strategyNodeKeyType: StrategyNodeKeyType,
            ): SNode {
                return SNode(
                    strategyNodeKeyType = strategyNodeKeyType,
                    children = ArrayList(),
                )
            }

            fun getMiddleStateNode(
                stateIds: HashSet<String>
            ): SNode {
                return getMiddleNode(StrategyNodeKeyTypeState(stateIds))
            }

            fun getMiddleConstraintNode(
                constraint: String
            ): SNode {
                return getMiddleNode(StrategyNodeKeyTypeVarConstraint(constraint))
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
        IDataExpand: IDataExpandHelper.IDataExpand,
        //用于过滤状态结点
        filterStateFun: (HashSet<String>) -> Boolean,
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
                debugger.pln("\tisMiddleNode,strategyNodeKeyType=${node.strategyNodeKeyType}")
                node.strategyNodeKeyType!!.let {
                    if (it is StrategyNodeKeyTypeState) {
                        //符合条件才继续往下找结点，不符合就不继续往下找结点
                        return@touch filterStateFun(it.stateIds)
                    } else {
                        assert(it is StrategyNodeKeyTypeVarConstraint)
                        if (it is StrategyNodeKeyTypeClockConstraint) {
                            val clockConstraint = it.key.toClockConstraint()
                            debugger.pln("clockConstraint=${clockConstraint}")
                            return@touch clockConstraint!!.ifMeet2(IDataExpand)
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