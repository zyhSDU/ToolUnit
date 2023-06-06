package view.tab

import helper.base.CopyHelper
import helper.LogHelper
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import view.TabConnect
import java.net.URL
import java.util.*

class StringView : TabConnect {
    val s1 = "暂停中，点击运行"
    val s2 = "运行中，点击暂停"
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val booleanPropertyBtn = SimpleBooleanProperty().apply {
            set(false)
            btn.text = s1
        }
        booleanPropertyBtn.addListener { observable, oldValue, newValue ->
            if (newValue) {
                btn.text = s2
            } else {
                btn.text = s1
            }
        }
        btn.onAction = EventHandler {
            booleanPropertyBtn.value = !booleanPropertyBtn.value
        }
        val thread = object : Thread() {
            override fun run() {
                super.run()
                var lastString = ""
                while (true) {
                    sleep(1000)
                    if (booleanPropertyBtn.value) {
                        val s = CopyHelper.getFromClipboard().trim()
                        if (s.isNotEmpty() && s != lastString) {
                            LogHelper.trace(s)
                            //
                            Platform.runLater {
                                val label = Label(s)
                                vb.children.add(label)
                            }
                            //
                            lastString = s
                        }
                    }
                }
            }
        }
        thread.start()

//        btn.onAction.handle(null)
    }

    @FXML
    lateinit var btn: Button

    @FXML
    lateinit var vb: VBox
}