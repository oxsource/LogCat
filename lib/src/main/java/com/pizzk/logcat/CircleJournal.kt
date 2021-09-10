package com.pizzk.logcat

object CircleJournal {

    external fun open(path: String, size: Long, decayFactor: Float)

    external fun sink(msg: String)

    external fun close()

    init {
        System.loadLibrary("circle-journal")
    }
}