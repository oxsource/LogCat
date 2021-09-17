package com.pizzk.logcat.crash

import android.os.Handler
import android.os.Looper
import com.pizzk.logcat.Logcat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * CrashLooper use a new thread looper to prevent process exit
 */
class CrashLooper {
    private val latch = CountDownLatch(1)

    internal fun loop() {
        Thread {
            Looper.prepare()
            val delayMs: Long = Logcat.config().crashDelayMs()
            quit(ms = delayMs)
            Looper.loop()
            latch.countDown()
        }.start()
    }

    internal fun await() {
        latch.await(Logcat.config().crashTimeoutSec, TimeUnit.SECONDS)
    }

    private fun quit(ms: Long) {
        val looper = Looper.myLooper() ?: return
        Handler(looper).postDelayed({ looper.quitSafely() }, ms.coerceAtLeast(0))
    }
}