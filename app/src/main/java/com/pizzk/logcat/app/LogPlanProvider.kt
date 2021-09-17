package com.pizzk.logcat.app

import android.app.Application
import android.util.Log
import com.pizzk.logcat.state.Plan
import com.pizzk.logcat.state.PlanProvider
import java.io.File

class LogPlanProvider : PlanProvider() {
    private val plan = Plan()
    private var init = false

    override fun extras(context: Application): Map<String, Any> {
        val map: MutableMap<String, Any> = mutableMapOf()
        map["modify-by"] = "mocks"
        return map
    }

    override fun pull(params: Map<String, Any>): Plan? {
        Log.d("LogSyncProvider", "params: $params")
        if (init) return null
        init = true
        plan.reportOnWifi = true
        return plan
    }

    override fun push(id: String, file: File): Boolean {
        super.push(id, file)
        file.renameTo(File("${file.absolutePath}.bak"))
        return true
    }
}