package helper

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import java.net.URL

object FXMLHelper {
    fun <F> load(url: URL): Pair<Parent, F> {
        val fxmlLoader = FXMLLoader(url)
        val parent = fxmlLoader.load<Parent>()
        val controller = fxmlLoader.getController<F>()
        return Pair(parent, controller)
    }
}