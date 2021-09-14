package com.pizzk.logcat.crash

import android.content.Context
import android.os.Looper
import android.os.Process
import com.pizzk.logcat.Logcat
import com.pizzk.logcat.log.Logger
import com.pizzk.logcat.state.States
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

internal object Crasher {
    private const val TAG = "RoselleCrash"

    fun setup(seconds: Long = 5, hints: (Context) -> Unit = {}) {
        val context: Context = States.context() ?: return
        val handler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val exp: Throwable = throwable ?: Exception("UncaughtException")
            val latch = CountDownLatch(1)
            val msg = "${context.packageName} crashed."
            Logcat.e(TAG, msg, exp)
            Logger.flush { latch.countDown() }
            Thread {
                Looper.prepare()
                hints(context)
                Looper.loop()
            }.start()
            latch.await(seconds, TimeUnit.SECONDS)
            handler?.uncaughtException(thread, exp)
            Process.killProcess(Process.myPid())
            exitProcess(status = -1)
        }
    }
}