package helper.scxml.scxml2.t2_traffic

import kotlin.random.Random

//遍历，12重循环嵌套太丑陋，递归又不会写，算了
object TrafficTest4 {
    class Individual(
        val codes: ArrayList<Int> = arrayListOf(),
    ) {
        companion object {
            fun getRandomOne(): Individual {
                val individual = Individual()
                val codes = individual.codes
                repeat(6) {
                    codes.add(Random.nextInt(3))
                }
                repeat(6) {
                    codes.add(Random.nextInt(2))
                }
                return individual
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val c1s = arrayListOf("bike", "car", "train")
        val c2s = arrayListOf("train_wait_train", "train_wait_back")
        val ss = ArrayList<Individual>()
        repeat(10) {
            ss.add(Individual.getRandomOne())
        }

        ss.map {
            println(it.codes)
        }
    }
}