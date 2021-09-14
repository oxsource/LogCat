package com.pizzk.logcat.app.provider

import android.util.Log
import com.pizzk.logcat.report.SyncProvider
import com.pizzk.logcat.state.Plan
import java.io.File

class LogSyncProvider : SyncProvider() {

    override fun fetch(params: String): Plan? {
        Log.d("LogSyncProvider", "params: $params")
        return super.fetch(params)
    }

    override fun upload(file: File) {
        super.upload(file)
    }
}