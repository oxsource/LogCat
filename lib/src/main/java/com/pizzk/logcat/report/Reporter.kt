package com.pizzk.logcat.report

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.pizzk.logcat.Logcat
import com.pizzk.logcat.log.Logger
import com.pizzk.logcat.state.Defaults
import com.pizzk.logcat.state.States
import com.pizzk.logcat.utils.NetworkStats
import org.jetbrains.anko.doAsync
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

internal object Reporter {
    private const val TAG = "Roselle.Reporter"
    private val working: AtomicBoolean = AtomicBoolean()
    private val active: AtomicBoolean = AtomicBoolean()
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val retryControl: RetryControl = RetryControl()

    private val checkCallback: Runnable = object : Runnable {
        override fun run() {
            intervalCheck()
            if (States.plan().id.isEmpty()) return stopCheck()
            handler.postDelayed(this, retryControl.get())
            retryControl.next()
        }
    }

    fun startCheck() {
        if (active.get()) return
        Log.d(TAG, "start check.")
        active.set(true)
        retryControl.reset()
        handler.post(checkCallback)
    }

    fun stopCheck() {
        if (!active.get()) return
        Log.d(TAG, "stop check.")
        active.set(false)
        handler.removeCallbacks(checkCallback)
    }

    private fun intervalCheck(): Boolean {
        Log.d(TAG, "interval checking...")
        if (States.plan().id.isEmpty()) return false
        if (working.get()) return false
        //use expires control whether submit
        val expires = States.plan().expires
        val stamp = System.currentTimeMillis()
        if (!Defaults.crashed() && stamp - expires < 0) return false
        Log.d(TAG, "interval check Defaults.crashed or stamp - expires > 0.")
        val failure: (Throwable) -> Unit = { it ->
            Log.e(TAG, "interval check submit exp: ${it.message}")
            it.printStackTrace()
        }
        val execute: () -> Unit = {
            runCatching(::submit).onFailure(failure)
            working.set(false)
        }
        Logger.flush {
            working.set(true)
            doAsync { execute() }
        }
        return true
    }

    @Throws(Exception::class)
    private fun submit() {
        Log.d(TAG, "try to submit.")
        if (States.plan().id.isEmpty()) throw Exception("plan is empty.")
        val delegate = Logcat.config().planProvider
        val context = States.context() ?: throw Exception("States.context is null")
        //network and data flow protect check
        val transport = NetworkStats.check(context)
        if (NetworkStats.Transport.NONE == transport) throw Exception("network disconnect.")
        if (States.plan().reportOnWifi && NetworkStats.Transport.WIFI != transport) {
            throw Exception("report must be on wifi network.")
        }
        //collect log and meta file
        val dump = Logger.dump() ?: throw Exception("dump zip file failed.")
        kotlin.runCatching {
            val success = delegate.push(States.plan().id, dump)
            if (!success) return@runCatching
            Log.d(TAG, "submit success.")
            //reset states after push success
            Defaults.crashed(value = false)
            States.plan().reset()
            Defaults.savePlan()
            //remove Logger.NAMESPACE dir
            val file: File = Logger.path(context).parentFile ?: return@runCatching
            if (file.exists()) file.deleteRecursively()
        }.onFailure {
            Log.e(TAG, "submit push exp: ${it.message}")
            it.printStackTrace()
        }
        if (dump.exists()) dump.delete()
    }
}