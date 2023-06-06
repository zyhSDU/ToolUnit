package helper

import helper.base.PrintHelper.StringTo.toPrintln
import kotlin.random.Random

object ReinforcementLearningHelper {
    object Test1 {
        class QLearning(
            private val numStates: Int,
            private val numActions: Int,
            private val alpha: Double = 0.1,
            private val gamma: Double = 0.9,
            private val epsilon: Double = 0.1
        ) {
            private val qTable: Array<DoubleArray> = Array(numStates) { DoubleArray(numActions) { 0.0 } }

            fun selectAction(state: Int): Int {
                // ε-greedy策略选择动作
                if (Random.nextDouble() < epsilon) {
                    // 随机选择动作
                    return Random.nextInt(numActions)
                } else {
                    // 选择价值最高的动作
                    return qTable[state].indexOf(qTable[state].maxOrNull()!!)
                }
            }

            fun updateQTable(state: Int, action: Int, reward: Double, nextState: Int) {
                // 更新Q值
                qTable[state][action] =
                    (1 - alpha) * qTable[state][action] + alpha * (reward + gamma * qTable[nextState].maxOrNull()!!)
            }
        }

        class Maze(val maze: Array<IntArray>) {
            private val numRows: Int = maze.size
            private val numCols: Int = maze[0].size
            private var playerRow: Int = 0
            private var playerCol: Int = 0
            private val endRow: Int = numRows - 1
            private val endCol: Int = numCols - 1

            fun start() {
                // 初始化Q表
                val qLearning = QLearning(numRows * numCols, 4)

                // 进行500次迭代
                repeat(50000) {
                    "repeat:${it}\n".toPrintln()
                    // 重置迷宫和玩家位置
                    reset()

                    // 当玩家到达终点或迭代次数超过1000时停止迭代
                    var steps = 0
                    while (playerRow != endRow || playerCol != endCol) {
                        // 选择动作
                        val state = playerRow * numCols + playerCol
                        val action = qLearning.selectAction(state)

                        // 执行动作
                        when (action) {
                            0 -> playerRow-- // 上
                            1 -> playerCol++ // 右
                            2 -> playerRow++ // 下
                            3 -> playerCol-- // 左
                        }

                        // 判断是否越界
                        if (playerRow < 0 || playerRow >= numRows || playerCol < 0 || playerCol >= numCols || maze[playerRow][playerCol] == 1) {
                            // 如果越界或碰到墙壁，回到上一个位置并受到惩罚
                            when (action) {
                                0 -> playerRow++ // 上
                                1 -> playerCol-- // 右
                                2 -> playerRow-- // 下
                                3 -> playerCol++ // 左
                            }
                            qLearning.updateQTable(state, action, -1.0, playerRow * numCols + playerCol)
                        } else {
                            // 如果没有越界，获得奖励并更新Q值
                            val nextState = playerRow * numCols + playerCol
                            if (playerRow == endRow && playerCol == endCol) {
                                // 到达终点，获得奖励1
                                qLearning.updateQTable(state, action, 1.0, nextState)
                            } else {
                                // 没有到达终点，获得奖励0
                                qLearning.updateQTable(state, action, 0.0, nextState)
                            }
                        }

//                    "step:${steps}\n".toPrintln()
                        // 更新步数
                        steps++
                        if (steps > 1000) {
                            break
                        }
                    }
                }

                // 运行学习后的策略
                reset()
                println("学习后的策略：")
                var steps = 0
                while (playerRow != endRow || playerCol != endCol) {
                    // 输出当前位置
                    println("(${playerRow + 1},${playerCol + 1})")

                    // 选择动作
                    val state = playerRow * numCols + playerCol
                    val action = qLearning.selectAction(state)

                    // 执行动作
                    when (action) {
                        0 -> playerRow-- // 上
                        1 -> playerCol++ // 右
                        2 -> playerRow++ // 下
                        3 -> playerCol-- // 左
                    }

                    // 判断是否越界
                    if (playerRow < 0 || playerRow >= numRows || playerCol < 0 || playerCol >= numCols || maze[playerRow][playerCol] == 1) {
                        // 如果越界或碰到墙壁，回到上一个位置并重新选择动作
                        when (action) {
                            0 -> playerRow++ // 上
                            1 -> playerCol-- // 右
                            2 -> playerRow-- // 下
                            3 -> playerCol++ // 左
                        }
                    }

                    //
                    steps++
                    if (steps > 10) {
                        break
                    }
                }
                println("(${playerRow + 1},${playerCol + 1})")
            }

            private fun reset() {
                playerRow = 0
                playerCol = 0
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val maze = arrayOf(
                intArrayOf(0, 0, 0, 1, 0),
                intArrayOf(1, 1, 0, 1, 0),
                intArrayOf(0, 0, 0, 0, 0),
                intArrayOf(0, 1, 1, 1, 1),
                intArrayOf(0, 0, 0, 0, 0)
            )
            val game = Maze(maze)
            game.start()
        }
    }

    object Test2 {
        // 定义状态和动作
        enum class State { S1, S2, S3 }
        enum class Action { A1, A2 }

        // 定义环境
        class Environment {
            fun getNextState(
                state: State,
                action: Action,
            ): State {
                // 根据当前状态和动作计算下一个状态
                return when (state) {
                    State.S1 -> when (action) {
                        Action.A1 -> State.S2
                        Action.A2 -> State.S3
                    }
                    State.S2 -> when (action) {
                        Action.A1 -> State.S1
                        Action.A2 -> State.S3
                    }
                    State.S3 -> when (action) {
                        Action.A1 -> State.S1
                        Action.A2 -> State.S2
                    }
                }
            }

            fun getReward(
                state: State,
                action: Action,
                nextState: State,
            ): Double {
                // 返回从当前状态采取动作后进入下一个状态所获得的奖励
                return when (nextState) {
                    State.S1 -> 0.0
                    State.S2 -> 1.0
                    State.S3 -> if (state == State.S1 && action == Action.A2) 5.0 else -1.0
                }
            }
        }

        // 定义代理
        class Agent(private val environment: Environment) {
            // 定义状态价值函数，初始值为0
            val values = hashMapOf(State.S1 to 0.0, State.S2 to 0.0, State.S3 to 0.0)

            // 更新状态价值函数
            fun updateValue(
                state: State,
                action: Action,
                nextState: State,
                discountFactor: Double,
            ) {
                val reward = environment.getReward(state, action, nextState)
                val nextValue = values[nextState] ?: 0.0
                values[state] = reward + discountFactor * nextValue
            }

            // 选择最优动作
            fun chooseAction(state: State): Action {
                val valueA1 = environment.getReward(
                    state,
                    Action.A1,
                    environment.getNextState(state, Action.A1)
                ) + values[environment.getNextState(state, Action.A1)]!!
                val valueA2 = environment.getReward(
                    state,
                    Action.A2,
                    environment.getNextState(state, Action.A2)
                ) + values[environment.getNextState(state, Action.A2)]!!
                return if (valueA1 > valueA2) Action.A1 else Action.A2
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val environment = Environment()
            val agent = Agent(environment)
            val discountFactor = 0.9
            for (i in 1..100000) {
                val state = State.values().random()
                val action = agent.chooseAction(state)
                val nextState = environment.getNextState(state, action)
                agent.updateValue(state, action, nextState, discountFactor)
            }
            println("State values: ${agent.values}")
        }
    }
}
