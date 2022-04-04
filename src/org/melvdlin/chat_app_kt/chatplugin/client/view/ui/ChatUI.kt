package org.melvdlin.chat_app_kt.chatplugin.client.view.ui

import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.melvdlin.chat_app_kt.chatplugin.client.ClientChatPlugin
import org.melvdlin.chat_app_kt.chatplugin.client.Model
import org.melvdlin.chat_app_kt.chatplugin.client.view.fx.DisplayableChatMessage
import org.melvdlin.chat_app_kt.chatplugin.client.view.fx.DisplayableMessage
import org.melvdlin.chat_app_kt.chatplugin.client.view.fx.SystemMessage
import org.melvdlin.chat_app_kt.chatplugin.client.view.fx.TextEntryBox
import java.util.LinkedList

class ChatUI(private val controller : ClientChatPlugin, private val model : Model) : Stage() {
    val root = VBox()

    val topBar = HBox()
    val infoLabel = Label()
    val topBarSpacer = Region()
    val exitButton = Button("Exit")

    val messageLog : ObservableList<DisplayableMessage> =
        FXCollections.synchronizedObservableList(
            FXCollections.observableList(LinkedList()))
    val messageLogView = ListView(messageLog)

    val messageEntryBox = TextEntryBox(true, "Send", "Send a message...")

    init {
        scene = Scene(root)

        root.children.addAll(topBar, messageLogView, messageEntryBox)
        topBar.children.addAll(infoLabel, topBarSpacer, exitButton)

        HBox.setHgrow(topBarSpacer, Priority.ALWAYS)
        HBox.setHgrow(messageEntryBox.textField, Priority.ALWAYS)

        exitButton.onAction = EventHandler {
            controller.exit()
        }

        messageEntryBox.submitButton.onAction = EventHandler {
            val body = messageEntryBox.text
            messageEntryBox.clear()

            controller.sendMessage(body) {
                messageLog += SystemMessage(body = "Error sending message:\n$body", error = true)
            }
        }

        model.messageLog.addListener(ListChangeListener {
            if (it.wasAdded()) {
                it.addedSubList.forEach { msg ->
                    messageLog += DisplayableChatMessage(msg)
                }
            }
            if (it.wasRemoved()) {
                messageLog.removeIf { msg ->
                    msg is DisplayableChatMessage && it.removed.contains(msg.msg)
                }
            }
        })
    }
}