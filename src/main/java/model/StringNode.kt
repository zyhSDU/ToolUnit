package model

import helper.base.StringHelper
import java.io.File

class StringNode(
        var value: String = "",
        children: ArrayList<StringNode> = ArrayList(),
        var parent: StringNode? = null
) {

    var children = children
        set(value) {
            field = value
            field.map {
                it.parent = this
            }
        }

    fun add(stringNode: StringNode) {
        children.add(stringNode)
        stringNode.parent = this
    }

    class SIJ(val s: String = "", val i: Int = 0, val j: Int = 0)

    fun trace(init: (SIJ) -> Unit = { _ -> }, i: Int = 0, j: Int = 0) {
        if (i >= 0) {
            init(SIJ(value, i, j))
        }
        children.withIndex().map { (j, it) ->
            it.trace(init, i + 1, j)
        }
    }

    companion object {
        fun form(stringListList: ArrayList<ArrayList<String>>): ArrayList<StringNode> {
            val resultList = ArrayList<StringNode>()
            stringListList.withIndex().map { (i, it) ->
                val stringNode = StringNode("node${i}").apply {
                    resultList.add(this)
                }
                it.map {
                    stringNode.add(StringNode(it))
                }
            }
            return resultList
        }

        fun newRootNode(list: ArrayList<StringNode>): StringNode {
            return StringNode().apply {
                children = list
            }
        }

        /**
         * out根源
         */
        fun traceList(list: ArrayList<StringNode>, init: (SIJ) -> Unit = { s -> }) {
            newRootNode(list).trace(init, -1)
        }

        /**
         * 打印
         */
        fun printList(list: ArrayList<StringNode>) {
            traceList(list) {
                val str = StringHelper.tabsString(it.i, it.s)
                println(str)
            }
        }

        /**
         * 保存
         */
        fun saveList(list: ArrayList<StringNode>, file: File) {
            //清空
            file.writeText("")
            //
            traceList(list, init = {
                val str = StringHelper.tabsString(it.i, it.s)
                file.appendText("$str\n")
            })
        }
    }
}