package com.pizzk.logcat.identifier

import android.content.Context

data class Software(
    var version: String = ""
) {
    constructor(context: Context) : this() {
        kotlin.runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            version = info.versionName
        }
    }
}