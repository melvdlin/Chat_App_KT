package org.melvdlin.chat_app_kt.plugins.client.chat.fx

import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox

class TextEntryBox(disableWhenBlank : Boolean) : HBox() {

    val textField = TextField()
    val submitButton = Button()

    constructor(disableWhenBlank : Boolean, buttonText : String) : this(disableWhenBlank) {
        submitButton.text = buttonText
    }

    constructor(disableWhenBlank : Boolean, buttonText : String, promptText : String) : this(disableWhenBlank, buttonText) {
        textField.promptText = promptText
    }

    init {
        children += textField
        children += submitButton

        if (disableWhenBlank) {
            textField.textProperty().addListener { _, _, newValue ->
                if (newValue == null) {
                    submitButton.isDisable = true
                    return@addListener
                }
                if (submitButton.isDisabled && newValue.isNotBlank()) {
                    submitButton.isDisable = false
                }
                if (!submitButton.isDisabled && newValue.isBlank()) {
                    submitButton.isDisable = true
                }
            }
            submitButton.isDisable = true
        }

        // enable firing via hitting the enter key in the text field
        textField.onKeyPressed = EventHandler { event ->
            if (event.code == KeyCode.ENTER) {
                submitButton.fire()
            }
        }

    }
}