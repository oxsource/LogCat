package com.pizzk.logcat.app.provider

import android.util.Log
import com.pizzk.logcat.report.SyncProvider
import com.pizzk.logcat.state.Plan
import java.io.File

class LogSyncProvider : SyncProvider() {
    private val plan = Plan()
    private var init = false

    override fun fetch(params: String): Plan? {
        Log.d("LogSyncProvider", "params: $params")
        if (init) return null
        init = true
        plan.loggable = true
        plan.reportable = true
        plan.reportWhileWifi = true
        return plan
    }

    override fun upload(file: File) {
        super.upload(file)
        file.renameTo(File("${file.absolutePath}.bak"))
    }
}