package org.melvdlin.chat_app_kt.chatplugin.client

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.melvdlin.chat_app_kt.chatplugin.shared.ChatMessage
import java.util.*

class Model {
    val messageLog : ObservableList<ChatMessage> =
        FXCollections.synchronizedObservableList(
            FXCollections.observableList(LinkedList()))

    var appState : AppState = AppState.UNINITIALIZED
}

enum class AppState {
    UNINITIALIZED,
    LOGIN,
    FETCH_LOG,
    CHATTING,
    TERMINATING,
    TERMINATED,
    ERROR,
}