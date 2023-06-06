package view

import javafx.fxml.Initializable
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.stage.Stage

interface TabConnect : Initializable {
    fun tabInit(
        ownerStage: Stage,
        tabPane: TabPane,
        tab: Tab,
    ) {
    }
}