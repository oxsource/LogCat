package com.pizzk.logcat.report

import android.app.Application
import com.pizzk.logcat.identifier.Device
import com.pizzk.logcat.identifier.Identifier
import com.pizzk.logcat.state.States
import com.pizzk.logcat.utils.JsonUtils
import java.text.SimpleDateFormat
import java.util.*

open class MetaProvider {
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE)

    open fun provide(context: Application): String {
        val map: MutableMap<String, Any> = mutableMapOf()
        map["ids"] = Identifier.uuid()
        map["alias"] = Identifier.getAlias()
        map["package"] = States.name()
        map["version"] = States.version()
        map["device"] = Identifier.device()
        map["memory"] = Device.Memory()
        map["datetime"] = sdf.format(Date())
        hook(map)
        return JsonUtils.json(map)
    }

    open fun hook(map: MutableMap<String, Any>): Unit = Unit
}