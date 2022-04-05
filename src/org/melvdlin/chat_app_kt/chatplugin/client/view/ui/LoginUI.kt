package org.melvdlin.chat_app_kt.chatplugin.client.view.ui

import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.melvdlin.chat_app_kt.chatplugin.client.ClientChatPlugin
import org.melvdlin.chat_app_kt.chatplugin.client.Model
import org.melvdlin.chat_app_kt.chatplugin.client.view.fx.TextEntryBox

class LoginUI(
    private val controller : ClientChatPlugin,
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
            val username = nameEntryBox.text
            nameEntryBox.submitButton.isDisable = true
            controller.login(username) {
                ErrorPopup(false, "Login as $username failed.")
                nameEntryBox.submitButton.isDisable = false
            }
        }
    }
}