package helper.tu

object TuHelper {
    object WuXiangHelper {
        class UnionFind(
            private val nodeNum: Int,
        ) {
            private val parent = IntArray(nodeNum) { it }

            fun find(x: Int): Int {
                if (parent[x] != x) {
                    parent[x] = find(parent[x])
                }
                return parent[x]
            }

            fun union(x: Int, y: Int) {
                val px = find(x)
                val py = find(y)
                if (px != py) {
                    parent[px] = py
                }
            }
        }

        fun hasCycle(
            edges: ArrayList<ArrayList<Int>>,
        ): Boolean {
            //n为节点数（即最大编号加1）
            val n = edges.maxOf { it.maxOrNull()!! } + 1
            val uf = UnionFind(n)
            for ((u, v) in edges) {
                if (uf.find(u) == uf.find(v)) {
                    // 如果 u 和 v 属于同一个集合，则说明存在环路。
                    return true
                }
                uf.union(u, v)
            }
            return false
        }
    }

    object YouXiangHelper {
        fun hasCycle(edges: ArrayList<ArrayList<Int>>): Boolean {
            // 构建邻接表
            val neighbors = Array(edges.size) { mutableListOf<Int>() }
            for ((u, v) in edges) {
                neighbors[u].add(v)
            }

            // 状态数组
            val status = IntArray(neighbors.size)

            fun dfs(node: Int): Boolean {
                status[node] = 1 // 标记为正在访问中
                for (neighbor in neighbors[node]) {
                    when (status[neighbor]) {
                        0 -> if (dfs(neighbor)) return true // 未访问过，则递归遍历它的邻居
                        1 -> return true // 访问中，即存在环路
                    }
                }
                status[node] = 2 // 标记为已访问
                return false
            }

            // 对每个节点分别进行深度优先搜索
            for (node in neighbors.indices) {
                if (status[node] == 0 && dfs(node)) return true
            }
            return false
        }

        @JvmStatic
        fun main(args: Array<String>) {
            // 无环图
            val edges1 = arrayListOf(
                arrayListOf(0, 1),
                arrayListOf(1, 2),
                arrayListOf(2, 3),
                arrayListOf(3, 4)
            )
            println(hasCycle(edges1)) // false

            // 存在环路
            val edges2 = arrayListOf(
                arrayListOf(0, 1),
                arrayListOf(1, 2),
                arrayListOf(2, 3),
                arrayListOf(3, 4),
                arrayListOf(4, 1)
            )
            println(hasCycle(edges2)) // true
        }
    }
}