package org.melvdlin.chat_app_kt.chatplugin.client.view.ui

import javafx.stage.Stage
import javafx.stage.StageStyle

abstract class ManagedStage(style : StageStyle = StageStyle.DECORATED) : Stage(style) {
    abstract val keepOpen : Boolean
}