package com.pizzk.logcat

import android.app.Application
import android.content.Context
import com.pizzk.logcat.crash.Crasher
import com.pizzk.logcat.log.PrintFormat
import com.pizzk.logcat.log.Logger
import com.pizzk.logcat.report.MetaProvider
import com.pizzk.logcat.report.Reporter
import com.pizzk.logcat.report.SyncProvider

object Controller {
    private var initialize: Boolean = false

    //configs
    var logFormat: PrintFormat = PrintFormat()
    var crashHints: (Context) -> Unit = {}
    var crashWaits: Long = 5
    var reportExtrasProvider: MetaProvider = MetaProvider()
    var reportSyncProvider: SyncProvider = SyncProvider()

    fun with(context: Application) {
        if (initialize) return
        Logger.setup(context, logFormat)
        Crasher.setup(context, crashWaits, crashHints)
        Reporter.setup(context, reportExtrasProvider)
        initialize = true
    }

    fun version(): String = "1.0"
}