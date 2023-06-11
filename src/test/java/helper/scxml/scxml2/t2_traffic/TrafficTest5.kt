package helper.scxml.scxml2.t2_traffic

import helper.base.DebugHelper.Debugger
import helper.base.DebugHelper.DebuggerList
import helper.base.FileHelper.mkdirs
import helper.base.PrintHelper.StringTo.toPrintln
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.StateDataRenEventSelector.Expand.toLearnedRenEventSelector
import helper.scxml.scxml2.t2_traffic.fun_strategy.FunStrategyHelper.StrIntStrIntLHM
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper.rEnvStateConstraintLHM
import helper.scxml.scxml2.t2_traffic.fun_strategy.FunStrategyHelper.taskRun
import helper.scxml.scxml2.t2_traffic.fun_strategy.StrategyTripleHelper
import java.io.File
import kotlin.random.Random

object TrafficTest5 {
    object Expand {
        object LHMExpand {
            fun <E> Map<E, Int>.getSortedMap(): Map<E, Int> {
                return entries.sortedByDescending {
                    it.value
                }.associate {
                    it.toPair()
                }
            }

            fun <E> Map<E, Int>.toPrintln(
            ) {
                this.map { (id, i) ->
                    println("${id}=${i}")
                }
            }

            fun Map<Int, Int>.toPrintln(
                population: Population,
            ) {
                this.map { (id, i) ->
                    println("${population.individuals[id]}=${i}")
                }
            }
        }
    }

    val c1s = arrayListOf("car", "bike", "train")
    val c2s = arrayListOf("train_wait_train", "train_wait_back")

    class Individual(
        val genes: ArrayList<Int> = arrayListOf(),
    ) {
        var oldFitness: Int? = null
        fun getFitness(
            repeatTimes: Int = 100,
            ifGetOldFitness: Boolean = false
        ): Int {
            if (ifGetOldFitness) return oldFitness!!
            val a4LHM = this.toStrIntStrIntLHM()
            var totalValue = 0
            repeat(repeatTimes) {
                val value = taskRun(
                    outA4LHM = null,
                    envStateConstraintLHM = rEnvStateConstraintLHM,
                    envEventLHM = StrategyTripleHelper.envEventLHM1,
                    getIRenEventSelectorFun = { scxmlTuple ->
                        a4LHM.toLearnedRenEventSelector(scxmlTuple)
                    },
                    debuggerList = DebuggerList(arrayListOf(Debugger(0))),
                )
                totalValue += value
            }
            oldFitness = totalValue
            return totalValue
        }

        fun toStrIntStrIntLHM(): StrIntStrIntLHM {
            val a4LHM = StrIntStrIntLHM.getInitialLHM()
            repeat(6) {
                a4LHM["Aalborg"]!![it]!![c1s[this.genes[it]]] = 1
            }
            repeat(6) {
                a4LHM["Wait"]!![it]!![c2s[this.genes[6 + it]]] = 1
            }
            return a4LHM
        }

        //交叉
        //交叉率，即在交叉操作中，两个父代进行交叉的概率。
        fun crossover(
            p2: Individual,
            crossoverRate: Double,
        ): Individual {
            val p1 = this
            val child = getRandomOne()

            for (i in 0 until p1.genes.size) {
                if (Random.nextDouble() < crossoverRate) {
                    child.genes[i] = p1.genes[i]
                } else {
                    child.genes[i] = p2.genes[i]
                }
            }

            return child
        }

        //突变
        //变异率，即在突变操作中，每个基因变异的概率。
        fun mutate(mutationRate: Double) {
            repeat(6) {
                if (Random.nextDouble() < mutationRate) {
                    this.genes[it] = Random.nextInt(c1s.size)
                }
            }
            repeat(6) {
                if (Random.nextDouble() < mutationRate) {
                    this.genes[6 + it] = Random.nextInt(c2s.size)
                }
            }
        }

        override fun toString(): String {
            return genes.toString()
        }

        companion object {
            fun getRandomOne(): Individual {
                val individual = Individual()
                val codes = individual.genes
                repeat(6) {
                    codes.add(Random.nextInt(c1s.size))
                }
                repeat(6) {
                    codes.add(Random.nextInt(c2s.size))
                }
                return individual
            }
        }
    }

    class Population(
        val individuals: ArrayList<Individual> = arrayListOf(),
    ) {
        fun evolvePopulation(
            elitismCount: Int,
            mutationRate: Double,
            crossoverRate: Double,
            tournamentSize: Int,
            evolveDirString: String,
            gIndex: Int,
        ) {
            val gDirString = "${evolveDirString}/g${gIndex}"
            gDirString.mkdirs()

            val newPopulation = mutableListOf<Individual>()

            for (i in 0 until elitismCount) {
                newPopulation.add(this.individuals[i])
            }

            for (i in elitismCount until this.individuals.size) {
                val parent1 = tournamentSelection(tournamentSize)
                val parent2 = tournamentSelection(tournamentSize)
                val child = parent1.crossover(parent2, crossoverRate)
                child.mutate(mutationRate)
                newPopulation.add(child)
            }

            this.individuals.clear()
            this.individuals.addAll(newPopulation)

            this.writeToFile(gDirString)
        }

        fun writeToFile(
            gDirString: String,
        ) {
            individuals.withIndex().map { (i, it) ->
                it.toStrIntStrIntLHM().writeToFile(File("${gDirString}/g${i}.txt"))
            }
        }

        fun tournamentSelection(
            tournamentSize: Int,
        ): Individual {
            val tournament = mutableListOf<Individual>()

            for (i in 0 until tournamentSize) {
                tournament.add(this.individuals[Random.nextInt(this.individuals.size)])
            }

            return tournament.maxByOrNull {
                it.getFitness()
            }!!
        }

        companion object {
            fun getRandomOne(size: Int): Population {
                val population = Population()
                repeat(size) {
                    population.individuals.add(Individual.getRandomOne())
                }
                return population
            }
        }

        override fun toString(): String {
            val sb = StringBuilder()
            individuals.map {
                sb.append(it.toString())
                sb.append("\n")
            }
            return sb.toString()
        }
    }

    fun f1(
        dirString: String,
        evolveIndex: Int,
    ) {
        val evolveDirString = "${dirString}/evolve${evolveIndex}"

        //变异率，即在突变操作中，每个基因变异的概率。
        val populationSize = 100
        //变异率，即在突变操作中，每个基因变异的概率。
        val mutationRate = 0.01
        //交叉率，即在交叉操作中，两个父代进行交叉的概率。
        val crossoverRate = 0.95
        //精英数量，即每一代中最适应的个体数量，它们直接复制到下一代中，保证最优解不被遗忘。
        val elitismCount = 2
        //锦标赛大小，即在选择操作中，每个锦标赛中包含的个体数量。每次选择操作会从锦标赛中选出最适应的个体作为父代进行交叉。
        val tournamentSize = 5

        val population = Population.getRandomOne(populationSize)

        for (i in 0 until 100) {
            population.evolvePopulation(
                elitismCount,
                mutationRate,
                crossoverRate,
                tournamentSize,
                evolveDirString,
                i,
            )
            println(
                "Generation $i: ${
                    population.individuals.maxByOrNull {
                        it.getFitness()
                    }!!.getFitness(ifGetOldFitness = true)
                }"
            )
        }

        population.toString().toPrintln()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        f1(
            "out/log/model/traffic/geneticModels",
            2,
        )
    }
}