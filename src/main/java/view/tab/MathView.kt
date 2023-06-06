package view.tab

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.VBox
import javafx.stage.Stage
import helper.MathematicaHelper
import view.TabConnect
import java.net.URL
import java.util.*

class MathView : TabConnect {
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        sendButton.onAction = EventHandler {
            MathematicaHelper.evaluate(inTextArea.text).run {
                showTextArea.text = this
            }
        }
        //初始聚焦
        Platform.runLater {
            inTextArea.requestFocus()
        }
    }

    //TabConnect
    override fun tabInit(ownerStage: Stage, tabPane: TabPane, tab: Tab) {
        ownerStage.scene.accelerators[KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN)] = Runnable {
            if (tabPane.selectionModel.selectedItem != tab) {
                return@Runnable
            }
            sendButton.onAction?.handle(null)
        }
    }

    @FXML
    lateinit var showTextArea: TextArea

    @FXML
    lateinit var inTextArea: TextArea

    @FXML
    lateinit var buttonsVBox: VBox

    @FXML
    lateinit var sendButton: Button
}