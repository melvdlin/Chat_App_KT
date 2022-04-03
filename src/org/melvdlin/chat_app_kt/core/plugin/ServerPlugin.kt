package org.melvdlin.chat_app_kt.core.plugin

import org.melvdlin.chat_app_kt.core.plugin.Plugin

interface ServerPlugin : Plugin {
    fun onServerStartup()
}