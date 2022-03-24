package org.melvdlin.chat_app_kt.plugins.client

import javafx.scene.Scene
import java.net.Socket

interface ClientPlugin {
    fun onConnected(server : Socket, primaryScene : Scene)
}