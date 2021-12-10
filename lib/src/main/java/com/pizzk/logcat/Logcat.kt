package com.pizzk.logcat

import android.app.Application
import com.pizzk.logcat.crash.Crasher
import com.pizzk.logcat.identifier.Identifier
import com.pizzk.logcat.log.Logger
import com.pizzk.logcat.report.Reporter
import com.pizzk.logcat.state.PlanProvider
import com.pizzk.logcat.state.States
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.concurrent.atomic.AtomicBoolean

object Logcat {
    class Config(
        var crashDelayMs: () -> Long = { 0 },
        var crashTimeoutSec: Long = 5,
        var planProvider: PlanProvider = PlanProvider(),
    ) {
        internal fun copy(v: Config) {
            crashDelayMs = v.crashDelayMs
            crashTimeoutSec = v.crashTimeoutSec
            planProvider = v.planProvider
        }
    }

    private val config = Config()
    private val fetching: AtomicBoolean = AtomicBoolean()

    @JvmStatic
    fun with(context: Application, config: Config) {
        if (!States.init(context)) return
        this.config.copy(config)
        Logger.setup()
        Crasher.setup()
        Reporter.startCheck()
    }

    internal fun config(): Config = config

    @JvmStatic
    fun setAlias(value: String) = Identifier.setAlias(value)

    @JvmStatic
    fun v(tag: String?, msg: String?, ex: Throwable? = null) = Logger.log(Logger.V, tag, msg, ex)

    @JvmStatic
    fun i(tag: String?, msg: String?, ex: Throwable? = null) = Logger.log(Logger.I, tag, msg, ex)

    @JvmStatic
    fun d(tag: String?, msg: String?, ex: Throwable? = null) = Logger.log(Logger.D, tag, msg, ex)

    @JvmStatic
    fun w(tag: String?, msg: String?, ex: Throwable? = null) = Logger.log(Logger.W, tag, msg, ex)

    @JvmStatic
    fun e(tag: String?, msg: String?, ex: Throwable? = null) = Logger.log(Logger.E, tag, msg, ex)

    @JvmStatic
    fun flush(finish: () -> Unit = {}) {
        Logger.flush(finish)
    }

    @JvmStatic
    fun fetch() {
        if (fetching.get()) return
        fetching.set(true)
        doAsync {
            runCatching { config().planProvider.fetch() }.onFailure { it.printStackTrace() }
            fetching.set(false)
            uiThread { Reporter.startCheck() }
        }
    }
}