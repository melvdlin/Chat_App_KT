package org.melvdlin.chat_app_kt.chatplugin.client.view.ui

import javafx.application.Platform
import javafx.stage.Stage

class StageManager(vararg stages : ManagedStage) {
    private val stages : MutableList<ManagedStage> = mutableListOf(*stages)

    fun add(stage : ManagedStage) = synchronized(stages) { stages.add(stage) }
    fun addAll(vararg stages : ManagedStage) = addAll(stages.toList())
    fun addAll(stages : Collection<ManagedStage>) = synchronized(this.stages) { this.stages.addAll(stages) }

    fun remove(stage : ManagedStage) = synchronized(this.stages) { stages.remove(stage) }
    fun removeAll(vararg stages : ManagedStage) = removeAll(stages.toList())
    fun removeAll(stages : Collection<ManagedStage>) = synchronized(this.stages) { this.stages.removeAll(stages) }

    fun closeStages() {
        Platform.runLater {
            synchronized(stages) {
                stages.forEach {
                    if (!it.keepOpen) {
                        it.close()
                    }
                }
            }
        }
    }
}