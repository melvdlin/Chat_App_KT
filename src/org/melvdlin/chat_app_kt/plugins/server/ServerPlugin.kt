package org.melvdlin.chat_app_kt.plugins.server

import org.melvdlin.chat_app_kt.plugins.Plugin

interface ServerPlugin : Plugin {
    fun onServerStartup()
}