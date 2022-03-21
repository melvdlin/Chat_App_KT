package org.melvdlin.chat_app_kt.server

import java.io.InputStream
import java.io.ObjectInputStream

class IncomingTrafficHandler(private val inputStream : InputStream, private val addTask : (ConnectionTask) -> Unit) : Thread() {

    override fun run() {
        ObjectInputStream(inputStream).use {
            while (!isInterrupted) {
                when (it.readObject()) {

                }
            }
        }
    }
}