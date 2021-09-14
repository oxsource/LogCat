package com.pizzk.logcat.state

import android.content.Context
import android.content.SharedPreferences

internal object Defaults {
    private var sp: SharedPreferences? = null

    private fun sp(): SharedPreferences? {
        if (null != sp) return sp
        val context = States.context() ?: return null
        sp = context.getSharedPreferences("Roselle-Defaults", Context.MODE_PRIVATE)
        return sp
    }

    fun loadPlan() {
        val sp: SharedPreferences = sp() ?: return
        val plan = States.plan()
        val sets = emptySet<String>()
        plan.loggable = sp.getBoolean("loggable@plan", false)
        plan.logLevels = sp.getStringSet("logLevels@plan", sets) ?: sets
        plan.reportable = sp.getBoolean("reportable@plan", false)
        plan.reportWhileWifi = sp.getBoolean("reportWhileWifi@plan", true)
    }

    fun savePlan() {
        val sp: SharedPreferences = sp() ?: return
        val plan = States.plan()
        sp.edit()
            .putBoolean("loggable@plan", plan.loggable)
            .putStringSet("logLevels@plan", plan.logLevels)
            .putBoolean("reportable@plan", plan.reportable)
            .putBoolean("reportWhileWifi@plan", plan.reportWhileWifi)
            .apply()
    }

    fun uuid(value: String) {
        val sp: SharedPreferences = sp() ?: return
        sp.edit()
            .putString("uuid@global", value)
            .apply()
    }

    fun uuid(): String {
        return sp()?.getString("uuid@global", "") ?: ""
    }
}