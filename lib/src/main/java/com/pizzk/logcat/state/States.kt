package com.pizzk.logcat.state

import android.app.Application

internal object States {
    private var app: Application? = null
    private val plan: Plan = Plan()

    fun init(value: Application): Boolean {
        if (null != app) return false
        app = value
        Defaults.loadPlan()
        return true
    }

    fun context(): Application? = app

    fun plan(): Plan = plan

    fun name(): String = app?.packageName ?: ""

    fun version(): String {
        val context = app ?: return ""
        return kotlin.runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            return info.versionName
        }.getOrDefault("")
    }
}