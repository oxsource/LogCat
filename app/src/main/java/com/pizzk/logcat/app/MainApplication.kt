package com.pizzk.logcat.app

import android.app.Application
import android.widget.Toast
import com.pizzk.logcat.Logcat

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val config = Logcat.Config()
        val crashText = "APP运行异常，程序即将退出"
        config.crashDelayMs = delay@{
            Toast.makeText(this, crashText, Toast.LENGTH_LONG).show()
            return@delay 1000
        }
        config.crashTimeoutSec = 5
        config.planProvider = LogPlanProvider()
        Logcat.with(this, config)
    }
}