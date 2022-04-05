package org.melvdlin.chat_app_kt.chatplugin.client.view.ui

import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage

class FetchingUI : ManagedStage() {
    
    override val keepOpen = false

    val root = VBox()
    val infoLabel = Label("Fetching message log...")

    init {
        scene = Scene(root)
        root.children += infoLabel
    }
}