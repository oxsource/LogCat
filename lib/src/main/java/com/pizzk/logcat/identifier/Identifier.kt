package com.pizzk.logcat.identifier

import com.pizzk.logcat.state.Defaults
import java.util.*

internal object Identifier {
    private var uuid = ""
    private var alias: String = ""
    private val extras: MutableMap<String, Any> = mutableMapOf()

    private val device: Device by lazy { Device() }

    fun setAlias(value: String) {
        alias = value
    }

    fun getAlias() = alias

    fun putExtra(key: String, value: Any): Identifier {
        if (key.isNotEmpty()) extras[key] = value
        return this
    }

    fun delExtra(key: String): Identifier {
        extras.remove(key)
        return this
    }

    fun getExtras(): Map<String, Any> {
        return extras.toMap()
    }

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