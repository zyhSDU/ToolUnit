package helper

import helper.base.PrintHelper.StringTo.toPrintln
import helper.scxml.scxml1.Scxml1Helper.DataExpand.exprAddOne
import helper.scxml.scxml1.Scxml1Helper.DataExpand.exprEqualsInt
import helper.scxml.scxml1.Scxml1Helper.DataExpand.getIntExpr
import helper.scxml.scxml1.Scxml1Helper.TransitionTargetExpand.touchFromRootToThis
import helper.scxml.strategy.ScxmlOneStrategyHelper.SNode
import helper.block.BlockHelper.Expand.BlockListTo.joinToBlock
import helper.block.BlockHelper.Expand.BlockTo.toLineBlock
import helper.block.LatexBlockHelper.LatexBlockFactory.Companion.toMathFormulaBlock
import helper.block.LatexBlockHelper.LatexBlockFactory.Companion.toUnderSetArrowBlock
import helper.block.ScxmlBlockHelper
import helper.scxml.scxml1.Scxml1Helper
import org.apache.commons.scxml.model.TransitionTarget

object ScxmlHelperTestRun {
    val bf = ScxmlBlockHelper.ScxmlBlockFactory.bf

    object TestT3StateMachine {
        class T3StateMachine : Scxml1Helper.StateMachine(
            "scxml/tests3/t3_block.scxml",
        )

        @JvmStatic
        fun main(args: Array<String>) {
            val t3sm = T3StateMachine()
            bf.getTexBlock(
                packages = arrayListOf("ctex"),
                title = "scxml",
                author = "张宇涵",
                date = "March 2023",
                documentContentBlock = bf.getDocumentBlockBlock(
                    bf.getSectionBlock(
                        "Introduction",
                        t3sm.toLatexBlock(),
                    )
                ),
            ).getStr().toPrintln()
            val es: ArrayList<ScxmlBlockHelper.ScxmlBlockFactory.Event> = arrayListOf()
            val cs: ArrayList<ScxmlBlockHelper.ScxmlBlockFactory.Configuration> = arrayListOf()
            cs.add(ScxmlBlockHelper.ScxmlBlockFactory.Configuration().also {
                it.blockStates.also { itStates ->
                    t3sm.engine.currentStatus.states.filterNotNull().map {
                        it as TransitionTarget
                        it.touchFromRootToThis {
                            itStates.add(t3sm.idStateLHM[it.id]!!)
                        }
                    }
                }
            })
            t3sm.testRun2(es, cs) {
                bf.getEmptyBlock(
                    bf.getNewLineBlock(),
                    bf.getEmptyBlock(
                        es.indices.map {
                            bf.getEmptyBlock(
                                cs[it].toSelfBlock(),
                                es[it].toLatexBlock().toUnderSetArrowBlock(),
                            )
                        }.joinToBlock(""),
                        cs[cs.size - 1].toSelfBlock(),
                    ).toMathFormulaBlock().toLineBlock(),
                ).printCode()
            }
        }
    }

    object T4 {
        class T4StateMachine : Scxml1Helper.StateMachine(
            "scxml/tests3/t4_traffic.scxml",
        ) {
            fun Aalborg() {

            }
        }

        object Test {
            fun runT4StateMachine(
                envStrategyNode: SNode,
                init: (T4StateMachine) -> Unit = {},
            ) {
                val t4StateMachine = T4StateMachine()
                t4StateMachine.printlnLatexBlockString()
                t4StateMachine.printlnStructBlockString()
                t4StateMachine.printlnCurrentConfigure()

                while (true) {
                    t4StateMachine.tryFire(envStrategyNode)
                    if (t4StateMachine.globalTime.exprEqualsInt(60)) {
                        break
                    }
                    if (t4StateMachine.isOnState("Sydney")) {
                        break
                    } else {
                        t4StateMachine.setDataExprAddOne("T")
                        t4StateMachine.globalTime.exprAddOne()
                        t4StateMachine.printlnCurrentConfigure()
                    }
                }
                init(t4StateMachine)
            }

            object Test2 {
                class Choose(
                    val state1: String,
                    val time1: Int,
                    val chosenEvent: String,
                ) {
                    override fun toString(): String {
                        return "Choose(state1='$state1', time1=$time1, chosenEvent='$chosenEvent')"
                    }
                }

                class ChooseTwo(
                    val c1: Choose,
                    val c2: Choose,
                ) {
                    override fun toString(): String {
                        return "ChooseTwo(c1=$c1, c2=$c2)"
                    }
                }

                class RunResult(
                    val runList: ArrayList<Int> = ArrayList(),
                    var countRunInTime: Int = 0,
                    var CountTotalInTime: Int = 0,
                ) : Comparable<RunResult> {
                    override fun compareTo(other: RunResult): Int {
                        return when {
                            countRunInTime != other.countRunInTime -> countRunInTime - other.countRunInTime
                            else -> CountTotalInTime - other.CountTotalInTime
                        }
                    }

                    override fun toString(): String {
                        return "RunResult(runList=$runList, countRunInTime=$countRunInTime, CountTotalInTime=$CountTotalInTime)"
                    }
                }

                fun getEnvStrategyNode(
                    chooseTwo: ChooseTwo,
                ): SNode {
                    val tt1 = chooseTwo.c1.time1
                    val tt2 = chooseTwo.c2.time1
                    val root = SNode.getRootNode()
                    root.addMiddleNode1(
                        "Aalborg", "0 <= T <= 2",
                        chooseTwo.c1.chosenEvent to 1.0,
                    )
                    root.addMiddleNode1(
                        "Bike", "42 <= T <= 45",
                        "bike_end" to 1.0,
                    )
                    root.addMiddleNode1(
                        "Car", "0 <= T <= 0",
                        "car_easy" to 10.0,
                        "car_heavy" to 1.0,
                    )
                    root.addMiddleNode1(
                        "Easy", "20 <= T <= 20",
                        "car_easy_end" to 1.0
                    )
                    root.addMiddleNode1(
                        "Heavy", "140 <= T <= 140",
                        "car_heavy_end" to 1.0
                    )
                    root.addMiddleNode1(
                        "Train", "4 <= T <= 6",
                        "train_go" to 10.0,
                        "train_wait" to 1.0
                    )
                    root.addMiddleNode1(
                        "Go", "35 <= T <= 35",
                        "train_go_end" to 1.0
                    )
                    root.addMiddleNode1(
                        "Wait", "${tt2} <= globalTime <= ${tt2}",
                        chooseTwo.c2.chosenEvent to 1.0
                    )
                    root.addMiddleNode1(
                        "GoBack", "0 <= T <= 0",
                        "back_back" to 1.0
                    )
                    return root
                }

                val totalTime = 60
                val state1 = "Aalborg"
                val state1Choose = arrayListOf(
                    "bike",
                    "car",
                    "train",
                )
                val state2 = "Wait"
                val state2Choose = arrayListOf(
                    "train_wait_train",
                    "train_wait_back",
                )

                fun main1() {
                    val chooseTwoList = ArrayList<ChooseTwo>()
                    state1Choose.map { s1c ->
                        (0 until totalTime).map { t1 ->
                            state2Choose.map { s2c ->
                                (0 until totalTime).map { t2 ->
                                    chooseTwoList.add(
                                        ChooseTwo(
                                            Choose(state1, t1, s1c),
                                            Choose(state2, t2, s2c),
                                        )
                                    )
                                }
                            }
                        }
                    }

                    chooseTwoList.map { chooseTwo ->
                        val runResult = RunResult()
                        repeat(1) {
                            val envStrategyNode = getEnvStrategyNode(chooseTwo)
                            runT4StateMachine(envStrategyNode) {
                                runResult.runList.add(it.globalTime.getIntExpr())
                            }
                        }
                        println("${chooseTwo}\t\t${runResult}")
                    }
                }

                fun main2() {
                    val chooseTwo1 = ChooseTwo(
                        Choose(state1, 1, state1Choose[0]),
                        Choose(state2, 0, state2Choose[0]),
                    )
                    val envStrategyNode = getEnvStrategyNode(chooseTwo1)
                    envStrategyNode.toBlock().getStr().toPrintln()
                    runT4StateMachine(envStrategyNode)
                }

                @JvmStatic
                fun main(args: Array<String>) {
                    main2()
                }
            }
        }
    }
}