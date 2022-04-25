package org.melvdlin.chat_app_kt.chatplugin.client.view.ui

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.melvdlin.chat_app_kt.chatplugin.client.ClientChatPlugin
import org.melvdlin.chat_app_kt.chatplugin.client.Controller
import org.melvdlin.chat_app_kt.chatplugin.client.Model
import org.melvdlin.chat_app_kt.chatplugin.client.view.fx.TextEntryBox
import java.util.concurrent.SynchronousQueue

class LoginUI(
    private val controller : Controller,
    private val model : Model)
    : ManagedStage() {

    override val keepOpen = false
    val root  = HBox()
    val nameEntryBox = TextEntryBox(true, "Login", "Enter a username...")

    val exitButton = Button("Exit")

    init {
        scene = Scene(root)
        root.children += nameEntryBox
        root.children += exitButton

        exitButton.onAction = EventHandler {
            controller.exit()
        }

        nameEntryBox.submitButton.onAction = EventHandler {
            val username = nameEntryBox.textField.text
            nameEntryBox.submitButton.isDisable = true
            controller.login(username) {
                Platform.runLater {
                    ErrorPopup(false, "Login as $username failed.").show()
                    nameEntryBox.submitButton.isDisable = false
                }
            }
        }
    }
}