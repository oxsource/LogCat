package com.pizzk.logcat.app

import android.app.Application
import android.widget.Toast
import com.pizzk.logcat.Logcat
import com.pizzk.logcat.app.provider.LogMetaProvider
import com.pizzk.logcat.app.provider.LogSyncProvider

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = Logcat.Config()
        val crashText = "APP运行异常，程序即将退出"
        config.crashHints = { ctx -> Toast.makeText(ctx, crashText, Toast.LENGTH_LONG).show() }
        config.crashWaits = 5
        config.reportMetaProvider = LogMetaProvider()
        config.reportSyncProvider = LogSyncProvider()
        Logcat.with(this, config)
    }
}