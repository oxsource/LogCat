package com.pizzk.logcat.crash

import android.os.Process
import com.pizzk.logcat.Logcat
import com.pizzk.logcat.state.Defaults
import kotlin.system.exitProcess

internal object Crasher {
    private const val TAG = "RoselleCrash"

    fun setup() {
        val handler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Defaults.crashed(value = true)
            val exp: Throwable = throwable ?: Exception("UncaughtException")
            Logcat.e(TAG, msg = "application crashed.", exp)
            val looper = CrashLooper()
            Logcat.flush(looper::loop)
            looper.await()
            handler?.uncaughtException(thread, exp)
            Process.killProcess(Process.myPid())
            exitProcess(status = -1)
        }
    }
}