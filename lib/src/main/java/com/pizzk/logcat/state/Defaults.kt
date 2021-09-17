package com.pizzk.logcat.state

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

internal object Defaults {
    private const val NAME = "Roselle-Defaults"
    private const val KEY_PLAN_ID = "id@plan"
    private const val KEY_PLAN_NAME = "name@plan"
    private const val KEY_PLAN_SECRET = "secret@plan"
    private const val KEY_PLAN_EXPIRED = "expired@plan"
    private const val KEY_PLAN_LOG_LEVELS = "logLevels@plan"
    private const val KEY_PLAN_REPORT_WIFI = "reportOnWifi@plan"
    private const val KEY_GLOBAL_UUID = "uuid@global"
    private const val KEY_CRASH_SIGNAL = "crash@signal"
    private var sp: SharedPreferences? = null

    private fun sp(): SharedPreferences? {
        if (null != sp) return sp
        val context = States.context() ?: return null
        sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return sp
    }

    fun loadPlan() {
        val sp: SharedPreferences = sp() ?: return
        val plan = States.plan()
        plan.id = sp.getString(KEY_PLAN_ID, "") ?: ""
        plan.name = sp.getString(KEY_PLAN_NAME, "") ?: ""
        plan.secret = sp.getString(KEY_PLAN_SECRET, "") ?: ""
        plan.expires = sp.getLong(KEY_PLAN_EXPIRED, 0)
        plan.logLevels = sp.getString(KEY_PLAN_LOG_LEVELS, "") ?: ""
        plan.reportOnWifi = sp.getBoolean(KEY_PLAN_REPORT_WIFI, true)
    }

    fun savePlan() {
        val sp: SharedPreferences = sp() ?: return
        val plan = States.plan()
        sp.edit {
            putString(KEY_PLAN_ID, plan.id)
            putString(KEY_PLAN_NAME, plan.name)
            putString(KEY_PLAN_SECRET, plan.secret)
            putLong(KEY_PLAN_EXPIRED, plan.expires)
            putString(KEY_PLAN_LOG_LEVELS, plan.logLevels)
            putBoolean(KEY_PLAN_REPORT_WIFI, plan.reportOnWifi)
        }
    }

    fun uuid(value: String? = null): String {
        val gets: () -> String = {
            sp()?.getString(KEY_GLOBAL_UUID, "") ?: ""
        }
        val s = value ?: return gets()
        sp()?.edit { putString(KEY_GLOBAL_UUID, s) }
        return gets()
    }

    fun crashed(value: Boolean? = null): Boolean {
        val gets: () -> Boolean = {
            sp()?.getBoolean(KEY_CRASH_SIGNAL, false) ?: false
        }
        val s = value ?: return gets()
        sp()?.edit { putBoolean(KEY_CRASH_SIGNAL, s) }
        return gets()
    }
}