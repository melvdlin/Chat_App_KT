package org.melvdlin.chat_app_kt.chatplugin.client.view.ui

import javafx.application.Platform
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
import javafx.util.Callback
import org.melvdlin.chat_app_kt.chatplugin.client.ClientChatPlugin
import org.melvdlin.chat_app_kt.chatplugin.client.Controller
import org.melvdlin.chat_app_kt.chatplugin.client.Model
import org.melvdlin.chat_app_kt.chatplugin.client.view.fx.*
import java.util.LinkedList
import java.util.concurrent.SynchronousQueue

class ChatUI(
    private val controller : Controller,
    private val model : Model
    ) : ManagedStage() {

    override val keepOpen = false

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
        //DEBUG
        println("Instantiating new ChatUI...")
        scene = Scene(root)

        root.children.addAll(topBar, messageLogView, messageEntryBox)
        topBar.children.addAll(infoLabel, topBarSpacer, exitButton)

        HBox.setHgrow(topBarSpacer, Priority.ALWAYS)
        HBox.setHgrow(messageEntryBox.textField, Priority.ALWAYS)

        exitButton.onAction = EventHandler {
            controller.exit()
        }

        messageLogView.cellFactory = Callback { DisplayableMessageListCell() }

        messageEntryBox.submitButton.onAction = EventHandler {
            val body = messageEntryBox.textField.text
            messageEntryBox.clear()

            controller.sendMessage(body) {
                Platform.runLater {
                    messageLog += SystemMessage(body = "Error sending message:\n$body", error = true)
                }
            }
        }

        model.messageLog.addListener(ListChangeListener {
            while (it.next()) {
                if (it.wasAdded()) {
                    it.addedSubList.forEach { msg ->
                        Platform.runLater {
                            messageLog += DisplayableChatMessage(msg)
                        }
                    }
                }
                if (it.wasRemoved()) {
                    Platform.runLater {
                        messageLog.removeIf { msg ->
                            msg is DisplayableChatMessage && it.removed.contains(msg.msg)
                        }
                    }
                }
            }
        })

        messageLog.addListener(ListChangeListener {
            Platform.runLater {
                messageLogView.scrollTo(messageLog.lastIndex)
            }
        })
    }
}