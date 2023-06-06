package helper.base

import kotlin.random.Random

object RandomHelper {
    // 这段代码定义了一个名为`getBooleanInProbability`的函数，它的作用是根据给定的概率返回一个布尔值。
    // 具体来说，当`Random.nextInt(probability)`的结果为0时，该函数返回`true`，否则返回`false`。
    //`Random.nextInt(n)`是Kotlin标准库中的一个函数，用于生成一个[0, n)范围内的随机整数。
    // 在这里，我们将`probability`作为参数传递给`Random.nextInt()`，
    // 因此它会生成一个[0, probability)范围内的随机整数。
    // 如果这个随机整数的值为0，那么`getBooleanInProbability`函数返回`true`，否则返回`false`。
    // 换句话说，当`probability`的值越小，函数返回`true`的概率越大，
    // 因为`Random.nextInt(probability)`返回0的概率就越高。
    // 如果`probability`的值为1，那么函数总是返回`true`；
    // 如果`probability`的值为2，函数返回`true`和`false`的概率各为50%；
    // 如果`probability`的值为3，函数返回`true`的概率为33.3%。
    fun getBooleanInProbability(
        probability: Int,
    ): Boolean {
        return Random.nextInt(probability) == 0
    }
}