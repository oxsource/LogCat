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
}