package view.tab

import helper.base.CopyHelper.copyToClipboard
import helper.LogHelper
import helper.base.RegexHelper
import helper.base.StringHelper.removeEmpty
import helper.base.StringHelper.removeEmptyLine
import helper.base.StringHelper.replaceMany
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.VBox
import javafx.stage.Stage
import view.TabConnect
import java.net.URL
import java.util.*

/**
 * @program: ToolUnit
 * @description:
 * @author: 张宇涵
 * @create: 2020-08-14 21:43
 */
class LogView : TabConnect {
    private var lastButton: Button? = null

    fun buttonAction(sb: StringBuilder, init: (sb: StringBuilder) -> Unit, suffix: (String) -> Unit) {
        init(sb)
        suffix(sb.toString())
    }

    //textArea_show文本框，追加
    fun buttonAction1(isClearTextAreaShow: Boolean = false, init: (sb: StringBuilder) -> Unit) {
        buttonAction(StringBuilder(), init) {
            if (isClearTextAreaShow) textArea_show.clear()
            textArea_show.appendText(it)
            it.run {
                LogHelper.trace(this)
            }
            it.copyToClipboard()
            textArea_in.clear()
            textArea_in.requestFocus()
        }
    }

    //textArea_show文本框，清屏后追加
    fun buttonAction2(init: (sb: StringBuilder) -> Unit) {
        buttonAction1(true, init)
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        button_send.mOnAction {
            buttonAction1 { stringBuilder ->
                textArea_in.text.apply {
                    if (this.isNotEmpty()) {
                        stringBuilder
                            .append("${"-".repeat(40)}\n")
                            .append(this + "\n")
                    }
                }
            }
        }
        button_javafxId.mOnAction {
            buttonAction2 { sb ->
                getInputText().split("\n").map {
                    RegexHelper.oldMatch("<([A-Z][a-zA-Z]*) fx:id=\"([a-zA-Z_0-9]+)\"", it)
                }.filter { (b, _) -> b }.map { (_, matcher) ->
                    sb.append("\t@FXML\n")
                        .append("\tlateinit var ${matcher.group(2)} : ${matcher.group(1)}\n")
                }
            }
        }
        button_removeEmptyLine.mOnAction {
            buttonAction2 { sb ->
                val s = getInputText().removeEmptyLine()
                sb.append(s)
            }
        }
        button_emergeLines.mOnAction {
            buttonAction2 { sb ->
                val s = getInputText().removeEmptyLine().replaceMany(LinkedHashMap<String, String>().apply {
                    this["\n"] = ""
                })
                sb.append(s)
            }
        }
        button_emergeLines_and_splitByDot.mOnAction {
            buttonAction2 { sb ->
                getInputText().removeEmptyLine()
                    .replaceMany(LinkedHashMap<String, String>().apply {
                        this["\n"] = " "
                        this["。"] = "。\n"
                        this[". "] = ".\n"
                        this["! "] = "!\n"
                        this["Fig.\n"] = "Fig."
                        this["Figs.\n"] = "Figs."
                        this["et al.\n"] = "et al. "
                        this["e.g.\n"] = "e.g. "
                        this["i.e.\n"] = "i.e. "
                        this[").\n"] = "). "
                        this["          "] = " "
                        this["        "] = " "
                        this["      "] = " "
                        this["    "] = " "
                        this["  "] = " "
                        this["？"] = "？\n"
                        this["A*"] = "\$A^*\$"
                        //
                    })
                    .run {
                        val sb = StringBuilder()
                        split("\n").map {
                            sb.append(it.trim()).append("\n")
                        }
                        sb.toString()
                    }
                    .run {
                        sb.append(this)
                    }
            }
        }
        button_emergeLines_and_splitByReferences.mOnAction {
            buttonAction2 { sb ->
                getInputText().removeEmptyLine()
                    .run {
                        val sb = StringBuilder()
                        split("\n").map {
                            // 改为正则表达式匹配，会比较高级
                            var if_begin_with_int = false
                            try {
                                val isi = it.split(". ")[0].trim().toInt()
                                if_begin_with_int = true
                            } catch (e: Exception) {
                            }
                            try {
                                val isi = it.split("[")[1].split("] ")[0].trim().toInt()
                                if_begin_with_int = true
                            } catch (e: Exception) {
                            }
                            if (if_begin_with_int) {
                                sb.append("aaaZhangYuHan")
                            }
                            sb.append(it.trim()).append("\n")
                        }
                        sb.toString()
                    }
                    .replaceMany(LinkedHashMap<String, String>().apply {
                        this["\n"] = " "
                        this["。"] = "。\n"
                        this[". "] = ".\n"
                        this["! "] = "!\n"
                        this["Fig.\n"] = "Fig."
                        this["Figs.\n"] = "Figs."
                        this["et al.\n"] = "et al. "
                        this["e.g.\n"] = "e.g. "
                        this["i.e.\n"] = "i.e. "
                        this[").\n"] = "). "
                        this["          "] = " "
                        this["        "] = " "
                        this["      "] = " "
                        this["    "] = " "
                        this["  "] = " "
                        this["？"] = "？\n"
                        this["A*"] = "\$A^*\$"
                        //
                    })
                    .run {
                        val sb = StringBuilder()
                        split("\n").map {
                            sb.append(it.trim()).append(" ")
                        }
                        sb.toString()
                    }
                    .replace("aaaZhangYuHan", "\n")
                    .trim()
                    .run {
                        sb.append(this)
                    }
            }
        }
        button_removeEmpty.mOnAction {
            buttonAction2 { sb ->
                getInputText().removeEmpty().run {
                    sb.append(this)
                }
            }
        }
        button_toChina.mOnAction {
            buttonAction2 { sb ->
                val s = getInputText().removeEmptyLine().replaceMany(LinkedHashMap<String, String>().apply {
                    this["\n"] = ""
                    this[" "] = ""
                    this["．"] = "."
                })
                sb.append(s)
            }
        }
        button_changeSpaceToBar.mOnAction {
            buttonAction2 { sb ->
                val s = getInputText().removeEmptyLine().replaceMany(LinkedHashMap<String, String>().apply {
                    this[" "] = "-"
                })
                sb.append(s)
            }
        }
        button_toSmall.mOnAction {
            buttonAction2 { sb ->
                val s = getInputText().removeEmptyLine().toLowerCase()
                sb.append(s)
            }
        }
        button_trim.mOnAction {
            buttonAction2 { sb ->
                sb.append(getInputText().trim())
            }
        }
        button_lastAction.onAction = EventHandler {
            lastButton?.onAction?.handle(null)
        }
        //初始聚焦
        Platform.runLater {
            textArea_in.requestFocus()
        }
        //默认上一次设置为
        button_emergeLines_and_splitByDot.onAction.handle(null)
    }

    //限下面类型的button，后续可能会添加，所以不仅于此
    /*
    send
    javafx id
    remover empty line
    emerge lines
     */
    private fun Button.mOnAction(init: () -> Unit) {
        onAction = EventHandler {
            init()
            lastButton = this
            button_lastAction.text = this.text
        }
    }

    //TabConnect
    override fun tabInit(ownerStage: Stage, tabPane: TabPane, tab: Tab) {
        ownerStage.scene.accelerators[KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN)] = Runnable {
            if (tabPane.selectionModel.selectedItem != tab) {
                return@Runnable
            }
            println(194)
            lastButton?.onAction?.handle(null)
            println(196)
        }
    }

    fun getInputText(): String {
        return if (textArea_in.text.isNotBlank() || !checkBox_supportContinuousOperation.isSelected) {
            textArea_in.text
        } else {
            textArea_show.text
        }
    }

    //
    @FXML
    lateinit var textArea_show: TextArea

    @FXML
    lateinit var textArea_in: TextArea

    @FXML
    lateinit var checkBox_clearTextAreaBeforeOutput: CheckBox

    @FXML
    lateinit var checkBox_supportContinuousOperation: CheckBox

    @FXML
    lateinit var button_send: Button

    @FXML
    lateinit var button_javafxId: Button

    @FXML
    lateinit var button_removeEmptyLine: Button

    @FXML
    lateinit var button_emergeLines: Button

    @FXML
    lateinit var button_emergeLines_and_splitByDot: Button

    @FXML
    lateinit var button_emergeLines_and_splitByReferences: Button

    @FXML
    lateinit var button_lastAction: Button

    @FXML
    lateinit var vBox_buttons: VBox

    @FXML
    lateinit var button_removeEmpty: Button

    @FXML
    lateinit var button_toChina: Button

    @FXML
    lateinit var button_changeSpaceToBar: Button

    @FXML
    lateinit var button_toSmall: Button

    @FXML
    lateinit var button_trim: Button
}