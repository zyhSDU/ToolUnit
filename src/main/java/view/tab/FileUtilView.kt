package view.tab

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import model.FileType
import helper.LogHelper
import helper.LogHelper.toTrace
import helper.MessageHelper
import helper.base.TimeHelper
import view.TabConnect
import java.io.File
import java.net.URL
import java.util.*

/**
 * @program: ToolUnit
 * @description:
 * @author: 张宇涵
 * @create: 2020-08-19 20:56
 */
class FileUtilView : TabConnect {
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        //
        val arrayOfNode: Array<Node> = arrayOf(textArea_fileContent, imageView_fileContent)
        val showNode: (Array<Node>, Node) -> Unit = { nodes, node ->
            nodes.map {
                (it == node).run {
                    it.isVisible = this
                    it.isManaged = this
                }
            }
        }
        //
        button_selectFile.onAction = EventHandler {
            DirectoryChooser().run {
                title = button_selectFile.text
                showDialog(ownerStage)
            }?.apply {
                textField_filePath.text = absolutePath
            }
        }
        textField_filePath.textProperty().addListener { observable, oldValue, newValue ->
            if (newValue == null) {
                return@addListener
            }
            File(newValue).apply {
                if (!this.exists()) {
                    return@addListener
                }
                treeTableView_fileTree.root = TreeItem(this)
                treeTableView_fileTree.refresh()
            }
        }
        treeTableView_fileTree.apply {
            columns.apply {
                (this[0] as TreeTableColumn<File, String>).setCellValueFactory {
                    SimpleStringProperty(it.value.value.run {
                        if (parentFile == null) {
                            absolutePath
                        } else {
                            name
                        }
                    })
                }
                (this[1] as TreeTableColumn<File, Boolean>).setCellValueFactory {
                    SimpleBooleanProperty(it.value.value.isDirectory)
                }
            }
            onMouseClicked = EventHandler {
                if (it.button == MouseButton.PRIMARY) {
                    when (it.clickCount) {
                        2 -> {
                            selectionModel.selectedItem.let { selectedItem ->
                                selectedItem?.value?.apply {
                                    if (isDirectory) {
                                        listFiles()?.map { TreeItem(it) }?.let {
                                            selectedItem.children.apply {
                                                clear()
                                                addAll(it)
                                                treeTableView_fileTree.refresh()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
                newValue?.value?.apply {
                    label_fileName.text = absolutePath
                    showNode(arrayOfNode, label_fileName)//作用：清空
                    if (isFile) {
                        FileType.FileTypeGroup.Image.doOne(name) {
                            imageView_fileContent.image = Image("file:${absolutePath}")
                            showNode(arrayOfNode, imageView_fileContent)
                        }
                        FileType.FileTypeGroup.Text.doOne(name) {
                            textArea_fileContent.text = readText()
                            showNode(arrayOfNode, textArea_fileContent)
                        }
                    }
                }
            }
        }
        button_replace.onAction = EventHandler {
            val textFrom = textField_replaceFrom.text
            val textTo = textField_replaceTo.text
            "${"-".repeat(30)}${TimeHelper.now()}".toTrace()
            treeTableView_fileTree.selectionModel.selectedItems.map { it.value }.map {
                if (checkBox_fileContent.isSelected) {
                    var isSuccessReplacing = true
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("replacing fileContent of ${it.absolutePath}\n")
                    stringBuilder.append("\tfrom\n")
                    stringBuilder.append("${textFrom}\n")
                    stringBuilder.append("\tto\n")
                    stringBuilder.append("${textTo}\n")
                    try {
                        it.writeText(it.readText().replace(textFrom, textTo))
                    } catch (e: Exception) {
                        isSuccessReplacing = false
                        e.printStackTrace()
                    }
                    stringBuilder.append("replace ${isSuccessReplacing}Successfully\n")
                    stringBuilder.toString().run {
                        LogHelper.trace(this)
                    }
                }
                if (checkBox_fileName.isSelected) {
                    it.let { fileFrom ->
                        "from：\t${fileFrom.absolutePath}".run {
                            LogHelper.trace(this)
                        }
                        var renameTo = false
                        if (fileFrom.parentFile == null) {
                            "to:\t\tfileFrom.parent==null".run {
                                LogHelper.trace(this)
                            }
                        } else {
                            val fileTo = File(fileFrom.parent, fileFrom.name.replace(textFrom, textTo))
                            renameTo = fileFrom.renameTo(fileTo)
                            "to:\t\t${fileTo.absolutePath}".run {
                                LogHelper.trace(this)
                            }
                        }
                        "${renameTo}Successfully${
                            if (renameTo) {
                                ""
                            } else {
                                "!".repeat(40)
                            }
                        }".run {
                            LogHelper.trace(this)
                        }
                    }
                }
            }
        }
        MessageHelper.mLogUtil.addListener {
            textArea_log.appendText("${it.string}\n")
        }
        //
        textArea_fileContent.contextMenu?.items?.add(MenuItem("保存").apply {
            this.onAction = EventHandler {
                treeTableView_fileTree.selectionModel.selectedItem.value?.apply {
                    var isSuccessSaving = true
                    try {
                        writeText(textArea_fileContent.text)
                    } catch (e: Exception) {
                        isSuccessSaving = false
                        e.printStackTrace()
                    }
                    "saving fileContent of ${absolutePath} ${isSuccessSaving}\n\tSuccessfully".run {
                        LogHelper.trace(this)
                    }
                }
            }
        })
    }

    private lateinit var ownerStage: Stage

    //TabPaneConnect
    override fun tabInit(ownerStage: Stage, tabPane: TabPane, tab: Tab) {
        this.ownerStage = ownerStage
    }

    //
    @FXML
    lateinit var button_selectFile: Button

    @FXML
    lateinit var textField_filePath: TextField

    @FXML
    lateinit var treeTableView_fileTree: TreeTableView<File>

    @FXML
    lateinit var textField_replaceFrom: TextField

    @FXML
    lateinit var textField_replaceTo: TextField

    @FXML
    lateinit var button_replace: Button

    @FXML
    lateinit var textArea_log: TextArea

    @FXML
    lateinit var label_fileName: Label

    @FXML
    lateinit var textArea_fileContent: TextArea

    @FXML
    lateinit var imageView_fileContent: ImageView

    @FXML
    lateinit var checkBox_fileName: CheckBox

    @FXML
    lateinit var checkBox_fileContent: CheckBox
}