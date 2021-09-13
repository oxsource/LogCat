package com.pizzk.logcat.crash

import android.app.Application
import android.content.Context
import android.os.Looper
import android.os.Process
import com.pizzk.logcat.log.Logger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

object Crasher {
    private const val TAG = "RoselleCrash"

    fun setup(context: Application, seconds: Long = 5, hints: (Context) -> Unit = {}) {
        val handler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val exp: Throwable = throwable ?: Exception("UncaughtException")
            val latch = CountDownLatch(1)
            val msg = "${context.packageName} crashed."
            Logger.e(TAG, msg, exp)
            Logger.destroy { latch.countDown() }
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