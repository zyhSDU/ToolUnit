package helper.base

import java.util.*

object HeapHelper {
    fun getIntMaxHeap(): PriorityQueue<Int> {
        return PriorityQueue<Int>(Collections.reverseOrder())
    }
}