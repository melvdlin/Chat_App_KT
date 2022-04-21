package org.melvdlin.chat_app_kt.core.netcode.impl

internal enum class HandlerState {
    UNINITIALIZED,
    IDLE,
    RUNNING,
    CLOSING,
    CLOSED,
    ERROR;

    fun ensureIsOneOf(vararg states : HandlerState) {
        if (!states.contains(this))
            throw IllegalStateException()
    }

    fun ensureIsNoneOf(vararg states : HandlerState) {
        if (states.contains(this))
            throw IllegalStateException()
    }
}