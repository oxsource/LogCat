package com.pizzk.logcat.identifier

import com.pizzk.logcat.state.Defaults
import java.util.*

internal object Identifier {
    private var uuid = ""
    private var alias: String = ""
    private val device: Device by lazy { Device() }

    fun setAlias(value: String) {
        alias = value
    }

    fun getAlias() = alias

    fun uuid(): String {
        //level 1
        if (uuid.isNotEmpty()) return uuid
        //obtain form cache
        uuid = Defaults.uuid()
        if (uuid.isNotEmpty()) return uuid
        //create new
        uuid = UUID.randomUUID().toString()
        Defaults.uuid(uuid)
        return uuid
    }

    fun device(): Device = device
}