package helper.base

import org.junit.Test

internal class HeapHelperTest {
    @Test
    fun t1() {
        // 创建一个空的 PriorityQueue
        val maxHeap = HeapHelper.getMaxHeap()

        // 添加元素到最大堆中
        maxHeap.add(3)
        maxHeap.add(1)
        maxHeap.add(5)
        maxHeap.add(2)

        // 输出最大堆中的前三个元素
        repeat(3) {
            println(maxHeap.remove())
        }
    }
}