package org.melvdlin.chat_app_kt.core.traffic

import java.security.SecureRandom

abstract class Request : Traffic {
    private companion object {
        val rng = SecureRandom()
    }

    var id : Long? = null
    set(value) {
        synchronized(this) {
            if (id != null)
                throw IllegalStateException("ID may only be set once.")
            if (value == null)
                throw NullPointerException()
            field = value
        }
    }
}