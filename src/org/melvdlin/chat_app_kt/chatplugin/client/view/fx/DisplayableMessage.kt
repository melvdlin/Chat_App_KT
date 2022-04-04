package org.melvdlin.chat_app_kt.chatplugin.client.view.fx

interface DisplayableMessage {
    val sender : String
    val body : String
    val timestamp : Long
}