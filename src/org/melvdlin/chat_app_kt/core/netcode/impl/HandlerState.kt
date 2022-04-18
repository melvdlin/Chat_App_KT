package org.melvdlin.chat_app_kt.core.netcode.impl

internal enum class HandlerState {
    UNINITIALIZED,
    IDLE,
    RUNNING,
    CLOSED,
    ERROR;

    fun ensureStates(vararg states : HandlerState) {
        if (synchronized(this) { !states.contains(this) }) throw IllegalStateException()
    }

    fun ensureNotStates(vararg states : HandlerState) {
        if (synchronized(this) { states.contains(this) }) throw IllegalStateException()
    }
}

