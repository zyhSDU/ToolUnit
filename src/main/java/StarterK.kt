import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import helper.FXMLHelper
import helper.base.PropertiesHelper
import view.MainView

/**
 * @program: ToolUnit
 * @description:
 * @author: 张宇涵
 * @create: 2020-08-14 21:37
 */
class StarterK : Application() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(StarterK::class.java)
        }
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun start(primaryStage: Stage) {
        val (parent, mainView) = FXMLHelper.load<MainView>(javaClass.getResource("/fxml/MainView.fxml"))
        primaryStage.run {
            scene = Scene(parent)
            mainView.lateInit(this)
            mainView.tabPane.selectionModel.select(PropertiesHelper.getProperty("firstViewIndex").toInt())
            show()
        }
    }
}