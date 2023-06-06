package helper.base

object FunctionHelper {
    class FunctionNode<E, F>(val function: (E) -> F) {
        var nextNode: FunctionNode<F, *>? = null
        fun run(e: E): F {
            return function(e)
        }

        fun runAll(e: E): Any? {
            val f = run(e)
            return if (nextNode == null) {
                f
            } else {
                nextNode!!.runAll(f)
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val f1 = FunctionNode<Pair<Int, String>, String> { (i, s) ->
            return@FunctionNode StringHelper.tabsString(i, s)
        }
        val f2 = FunctionNode<String, String> {
            println(it)
            it + "2"
        }
        val f3 = FunctionNode<String, Unit> {
            println(it)
        }
        f1.nextNode = f2
        f2.nextNode = f3
        f1.runAll(Pair(1, "hh"))
    }
}