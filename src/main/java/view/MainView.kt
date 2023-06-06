package view

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.stage.Stage
import view.tab.FileUtilView
import view.tab.LogView
import view.tab.MathView
import view.tab.StringView
import java.net.URL
import java.util.*

/**
 * @program: ToolUnit
 * @description:
 * @author: 张宇涵
 * @create: 2020-08-14 21:37
 */
class MainView : Initializable {
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        mathTab.setOnSelectionChanged {
            if (mathTab.isSelected) {
                "mathTab.isSelected = ${mathTab.isSelected}".run {
                    println(this)
                }
            }
        }
    }

    fun lateInit(stage: Stage) {
        arrayListOf(
            Pair(fileUtilTab, fileUtilViewController),
            Pair(logTab, logViewController),
            Pair(stringTab, stringViewController),
            Pair(mathTab, mathViewController),
        ).map { (tab, controller) ->
            controller.tabInit(stage, tabPane, tab)
        }
    }

    @FXML
    lateinit var tabPane: TabPane

    @FXML
    lateinit var fileUtilTab: Tab

    @FXML
    lateinit var logTab: Tab

    @FXML
    lateinit var stringTab: Tab

    @FXML
    lateinit var mathTab: Tab

    @FXML
    lateinit var fileUtilViewController: FileUtilView

    @FXML
    lateinit var logViewController: LogView

    @FXML
    lateinit var stringViewController: StringView

    @FXML
    lateinit var mathViewController: MathView
}