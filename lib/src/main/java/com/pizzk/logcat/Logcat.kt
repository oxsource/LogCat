package com.pizzk.logcat

import android.app.Application
import android.content.Context
import com.pizzk.logcat.crash.Crasher
import com.pizzk.logcat.identifier.Identifier
import com.pizzk.logcat.log.Logger
import com.pizzk.logcat.report.MetaProvider
import com.pizzk.logcat.report.Reporter
import com.pizzk.logcat.report.SyncProvider
import com.pizzk.logcat.state.Plan
import com.pizzk.logcat.state.States
import org.jetbrains.anko.doAsync

object Logcat {
    class Config {
        var crashHints: (Context) -> Unit = {}
        var crashWaits: Long = 5
        var reportMetaProvider: MetaProvider = MetaProvider()
        var reportSyncProvider: SyncProvider = SyncProvider()
    }

    fun with(context: Application, config: Config) {
        if (!States.init(context)) return
        Crasher.setup(config.crashWaits, config.crashHints)
        Reporter.setup(config.reportMetaProvider, config.reportSyncProvider)
        submitIfy()
    }

    fun version(): String = "1.0"

    fun alias(value: String) = Identifier.setAlias(value)

    fun plan(): Plan = States.plan()

    fun submitIfy() {
        Logger.flush { doAsync { Reporter.fetch().submit() } }
    }

    fun v(tag: String?, msg: String?, ex: Throwable? = null) = Logger.log(Logger.V, tag, msg, ex)

    fun i(tag: String?, msg: String?, ex: Throwable? = null) = Logger.log(Logger.I, tag, msg, ex)

    fun d(tag: String?, msg: String?, ex: Throwable? = null) = Logger.log(Logger.D, tag, msg, ex)

    fun w(tag: String?, msg: String?, ex: Throwable? = null) = Logger.log(Logger.W, tag, msg, ex)

    fun e(tag: String?, msg: String?, ex: Throwable? = null) = Logger.log(Logger.E, tag, msg, ex)
}