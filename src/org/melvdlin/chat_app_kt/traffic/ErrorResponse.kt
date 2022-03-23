package org.melvdlin.chat_app_kt.traffic

interface ErrorNotification : Traffic {
    val fatal : Boolean
    val info : String
}