package com.pizzk.logcat.report

import android.app.Application
import com.pizzk.logcat.identifier.Hardware
import com.pizzk.logcat.identifier.Identifier
import com.pizzk.logcat.identifier.Software
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MetaProvider {
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE)

    fun provide(context: Application): String {
        val map: MutableMap<String, Any> = mutableMapOf()
        map["ids"] = Identifier.getIds()
        map["alias"] = Identifier.getAlias()
        map["package"] = context.packageName
        map["version"] = version(context)
        map["device"] = Hardware()
        map["application"] = Software(context)
        map["datetime"] = sdf.format(Date())
        return kotlin.runCatching {
            val jbt = JSONObject(map.toMap())
            return@runCatching jbt.toString()
        }.getOrDefault("")
    }

    private fun version(context: Application): String {
        return kotlin.runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            return info.versionName
        }.getOrDefault("")
    }
}