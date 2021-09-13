package com.pizzk.logcat.log

internal object Roselle {
    external fun setup(path: String, size: Long)

    external fun sink(msg: String, length: Int)

    external fun flush()

    init {
        System.loadLibrary("roselle")
    }
}