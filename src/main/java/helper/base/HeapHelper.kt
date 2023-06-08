package helper.base

import java.util.*

object HeapHelper {
    fun getMaxHeap(): PriorityQueue<Int> {
        return PriorityQueue<Int>(Collections.reverseOrder())
    }
}