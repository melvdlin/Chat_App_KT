package org.melvdlin.chat_app_kt.chatplugin.client.view.ui

import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Stage

class ErrorPopup(
    val fatal : Boolean,
    val info : String,
    val onConfirmed : () -> Unit = {}
) : ManagedStage() {

    override val keepOpen = true

    private val root = VBox()
    private val topLabel = Label("A${if (fatal) " fatal" else "n"} error occured:")
    private val infoLabel = Label(info)
    private val okButtonBox = HBox()
    private val verticalSpacer = Region()
    private val okButtonSpacer1 = Region()
    private val okButtonSpacer2 = Region()
    private val okButton = Button("Ok")

    init {
        title = "${if (fatal) "Fatal " else ""}Error"

        scene = Scene(root)

        root.children.addAll(topLabel, infoLabel, verticalSpacer, okButtonBox)
        okButtonBox.children.addAll(okButtonSpacer1, okButton, okButtonSpacer2)

        VBox.setVgrow(verticalSpacer, Priority.ALWAYS)
        HBox.setHgrow(okButtonSpacer1, Priority.ALWAYS)
        HBox.setHgrow(okButtonSpacer2, Priority.ALWAYS)

        okButton.onAction = EventHandler {
            hide()
            onConfirmed()
        }
    }
}
