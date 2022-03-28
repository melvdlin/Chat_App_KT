package org.melvdlin.chat_app_kt.plugins.client

import javafx.scene.Scene
import org.melvdlin.chat_app_kt.plugins.Plugin

interface ClientPlugin : Plugin {
    fun onClientStartup()
}