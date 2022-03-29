package org.melvdlin.chat_app_kt.plugins.client.chat.fx

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.melvdlin.chat_app_kt.plugins.server.chat.ChatMessage
import java.time.Instant
import java.time.ZoneId

class ChatMessageListCell : ListCell<ChatMessage>() {
    private val messageBox = VBox()
    private val titleBox = HBox()
    private val sender = Label()
    private val timestamp = Label()
    private val body = Label()

    init {
        configureBoxes()
        assemble()
    }

    private fun configureBoxes() {
        messageBox.padding = Insets(5.0, 0.0, 5.0, 0.0)
        titleBox.padding = Insets(0.0, 5.0, 0.0, 5.0)
    }

    private fun assemble() {
        messageBox.children += titleBox
        messageBox.children += body
        titleBox.children += sender
        titleBox.children += timestamp
    }

    override fun updateItem(msg : ChatMessage?, empty : Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            clearContent()
        } else {
            addContent(msg!!)
        }
    }

    private fun clearContent() {
        text = null
        graphic = null
    }

    private fun addContent(msg : ChatMessage) {
        text = null
        sender.text = msg.sender
        timestamp.text = Instant.ofEpochMilli(msg.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime().toString()
        body.text = msg.body
        graphic = messageBox
    }
}