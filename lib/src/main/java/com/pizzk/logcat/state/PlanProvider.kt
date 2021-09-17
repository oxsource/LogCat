package com.pizzk.logcat.state

import android.app.Application
import com.pizzk.logcat.identifier.Device
import com.pizzk.logcat.identifier.Identifier
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws

open class PlanProvider {
    companion object {
        const val KEY_DEVICE = "device"
        const val KEY_UUID = "uuid"
        const val KEY_ALIAS = "alias"
        const val KEY_PACKAGE = "package"
        const val KEY_VERSION = "version"
    }

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE)

    fun metas(context: Application): Map<String, Any> {
        val map: MutableMap<String, Any> = mutableMapOf()
        map[KEY_DEVICE] = Identifier.device()
        map[KEY_UUID] = Identifier.uuid()
        map[KEY_PACKAGE] = States.name()
        map[KEY_VERSION] = States.version()
        map[KEY_ALIAS] = Identifier.getAlias()
        map["memory"] = Device.Memory()
        map["datetime"] = sdf.format(Date())
        map["extras"] = extras(context)
        return map
    }

    @Throws(Exception::class)
    fun fetch() {
        val maps: MutableMap<String, Any> = mutableMapOf()
        maps.putAll(Identifier.getExtras())
        maps[KEY_DEVICE] = Identifier.device()
        maps[KEY_UUID] = Identifier.uuid()
        maps[KEY_PACKAGE] = States.name()
        maps[KEY_VERSION] = States.version()
        maps[KEY_ALIAS] = Identifier.getAlias()
        val value = pull(maps) ?: return
        States.plan().of(value)
        Defaults.savePlan()
    }

    open fun extras(context: Application): Map<String, Any> = emptyMap()

    open fun pull(params: Map<String, Any>): Plan? = null

    open fun push(id: String, file: File): Boolean = false
}