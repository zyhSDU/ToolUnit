package helper.base

object BaseTypeHelper {
    fun Boolean.toInt(): Int {
        return if (this) {
            1
        } else {
            0
        }
    }

    fun Int.toBoolean(): Boolean {
        return this != 0
    }

    fun Int.toSting(): String {
        return "$this"
    }

    object ListExpand {
        fun ArrayList<Int>.toStringArrayList(): ArrayList<String> {
            val arr = ArrayList<String>()
            map {
                arr.add("$it")
            }
            return arr
        }

        fun <E> List<E>.toArrayList(): ArrayList<E> {
            val re = ArrayList<E>()
            map {
                re.add(it)
            }
            return re
        }

        fun <E> Array<out E>.toArrayList(): ArrayList<E> {
            return this.toList().toArrayList()
        }

        fun ArrayList<Double>.getMinValueKey(): Int? {
            var minValue = Double.MAX_VALUE
            var minIndex: Int? = null
            for ((index, value) in this.withIndex()) {
                if (value <= minValue) {
                    minValue = value
                    minIndex = index
                }
            }
            return minIndex
        }
    }

    object LHMExpand {
        object StringIntExpand {
            fun LinkedHashMap<String, Int>.getMaxKey(): String? {
                var maxKey: String? = null
                var maxValue = Int.MIN_VALUE
                for ((key, value) in this) {
                    if (value >= maxValue) {
                        maxKey = key
                        maxValue = value
                    }
                }
                return maxKey
            }
        }

        object StringDoubleExpand {
            fun <E> LinkedHashMap<E, Double>.getMinKey(): E? {
                var minKey: E? = null
                var minValue = Double.MAX_VALUE
                for ((key, value) in this) {
                    if (value <= minValue) {
                        minKey = key
                        minValue = value
                    }
                }
                return minKey
            }

            fun LinkedHashMap<String, Double>.getMaxKey(): String? {
                var maxKey: String? = null
                var maxValue = Double.MIN_VALUE
                for ((key, value) in this) {
                    if (value >= maxValue) {
                        maxKey = key
                        maxValue = value
                    }
                }
                return maxKey
            }
        }
    }
}