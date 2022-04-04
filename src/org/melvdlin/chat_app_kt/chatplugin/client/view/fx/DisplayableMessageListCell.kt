package org.melvdlin.chat_app_kt.chatplugin.client.view.fx

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import java.time.Instant
import java.time.ZoneId

class DisplayableMessageListCell : ListCell<DisplayableMessage>() {
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
//        messageBox.padding = Insets(5.0, 0.0, 5.0, 0.0)
//        titleBox.padding = Insets(0.0, 5.0, 0.0, 0.0)
        timestamp.padding = Insets(0.0, 0.0, 0.0, 5.0)
        sender.isUnderline = true
    }

    private fun assemble() {
        messageBox.children += titleBox
        messageBox.children += body
        titleBox.children += sender
        titleBox.children += timestamp
    }

    override fun updateItem(msg : DisplayableMessage?, empty : Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            clearContent()
        } else {
            msg?.let {
                addContent(msg)
            }
        }
    }

    private fun clearContent() {
        text = null
        graphic = null
    }

    private fun addContent(msg : DisplayableMessage) {
        text = null
        sender.text = msg.sender
        timestamp.text = Instant.ofEpochMilli(msg.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime().toString()
        body.text = msg.body
        graphic = messageBox

        if (msg is SystemMessage) {
            sender.font = Font.font(sender.font.family, FontWeight.BOLD, FontPosture.REGULAR, sender.font.size)
            if (msg.error) {
                val errorColor = Color.web(SystemMessage.defaultErrorColor)
                sender.textFill = errorColor
                body.textFill = errorColor
                timestamp.textFill = errorColor
            }
        }
    }
}