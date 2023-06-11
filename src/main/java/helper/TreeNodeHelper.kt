package helper

import helper.base.LogHelper
import helper.base.MathHelper.modifiedSubtract
import helper.base.TimeHelper
import java.lang.StringBuilder

object TreeNodeHelper {
    @JvmStatic
    fun main(args: Array<String>) {
        (0..10000).map {
            TimeHelper.nowCount1000().run {
                println("@name${this}")
            }
        }

    }

    open class TreeNode<E>(
        var name: String = TimeHelper.nowCount1000(),//也可以用ObjectId()。import org.bson.types.ObjectId
        var element: E,
        private var children: LinkedHashSet<TreeNode<E>> = LinkedHashSet(),
        private var parent: LinkedHashSet<TreeNode<E>> = LinkedHashSet(),
    ) {
        companion object {
            fun <D> crete(
                name: String = TimeHelper.nowCount1000(),
                element: D,
                children: LinkedHashSet<TreeNode<D>> = LinkedHashSet(),
                parent: LinkedHashSet<TreeNode<D>> = LinkedHashSet(),
            ): TreeNode<D> {
                return TreeNode(name, element, children, parent)
            }

            //（crete函数）的例子
            fun creteInt(
                element: Int,
                children: LinkedHashSet<TreeNode<Int>> = LinkedHashSet(),
                parent: LinkedHashSet<TreeNode<Int>> = LinkedHashSet(),
            ): TreeNode<Int> {
                return crete(element = element, children = children, parent = parent)
            }

            object Test {
                @JvmStatic
                fun main(args: Array<String>) {
                    val a1 = creteInt(1)
                    a1.apply {
                        addChildren(
                            linkedSetOf(
                                creteInt(11).apply {
                                    addChildren(linkedSetOf(creteInt(111), creteInt(112), a1))
                                },
                                creteInt(12).apply {
                                    addChildren(linkedSetOf(creteInt(121), creteInt(122)))
                                },
                            )
                        )
                    }.run {
                        touch()
                    }
                }
            }
        }

        //add
        fun addChild(child: TreeNode<E>) {
            this.children.add(child)
            child.parent.add(this)
        }

        fun addChildren(children: LinkedHashSet<TreeNode<E>>) {
            children.map {
                this.addChild(it)
            }
        }

        fun addParent(parent: LinkedHashSet<TreeNode<E>>) {
            parent.map {
                it.addChild(this)
            }
        }

        //remove
        fun removeChild(child: TreeNode<E>) {
            this.children.remove(child)
            child.parent.remove(this)
        }

        fun removeChildren(children: LinkedHashSet<TreeNode<E>>) {
            children.map {
                this.removeChild(it)
            }
        }

        fun removeParent(parent: LinkedHashSet<TreeNode<E>>) {
            parent.map {
                it.removeChild(this)
            }
        }

        //touch
        fun touch(
            depth: Int = 0,
            touchedSet: LinkedHashSet<TreeNode<E>> = LinkedHashSet(),
            touched: Boolean = touchedSet.contains(this),
            sb: StringBuilder = StringBuilder(),
            doSome: (Int, Boolean, String) -> Unit = { depth, touched, name ->
                StringBuilder().run {
                    append("\t".repeat(depth.modifiedSubtract(1)!!))
                    append(if (depth > 0) "|---" else "")
                    append(if (touched) "(touched)" else "")
                    append(name)
                    append("\n")
                }?.run {
                    LogHelper.debug(this.toString(), LogHelper.LogLabelGroup.debug1)
                    sb.append(this.toString())
                }
            }
        ) {
            doSome(depth, touched, name)
            if (!touched) {
                touchedSet.add(this)
                children.map {
                    it.touch(depth + 1, touchedSet, sb = sb, doSome = doSome)
                }
            }
            if (depth == 0) {
                LogHelper.trace(sb.toString(), LogHelper.LogLabelGroup.trace1)
            }
        }
    }
}