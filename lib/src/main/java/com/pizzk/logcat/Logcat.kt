package com.pizzk.logcat

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import java.io.File

object Logcat {
    private const val NAMESPACE = "pizzk-logs"
    private val callback = Callback()
    private var handler: Handler? = null
    private var logFmt: LogFormat? = null
    private val allowLevels: MutableList<String> = listOf("W", "E").toMutableList()

    private val logsMap: Map<String, (String?, String?, Throwable?) -> Unit> = mapOf(
        Pair("V", { t, m, e -> Log.v(t, m, e) }),
        Pair("I", { t, m, e -> Log.i(t, m, e) }),
        Pair("D", { t, m, e -> Log.d(t, m, e) }),
        Pair("W", { t, m, e -> Log.w(t, m, e) }),
        Pair("E", { t, m, e -> Log.e(t, m, e) }),
    )

    fun active(context: Context, fmt: LogFormat = LogFormat()) {
        logFmt = fmt
        kotlin.runCatching {
            val thread = HandlerThread(NAMESPACE)
            thread.start()
            val handler = Handler(thread.looper, callback)
            val msg = handler.obtainMessage(Callback.WHAT_OPEN)
            msg.obj = path(context.applicationContext).absolutePath
            handler.sendMessage(msg)
            Logcat.handler = handler
        }.onFailure { it.printStackTrace() }
    }

    fun setAllowLevels(vararg levels: String) {
        allowLevels.clear()
        allowLevels.addAll(levels.toList())
    }

    fun close() {
        val handler = Logcat.handler ?: return
        kotlin.runCatching {
            if (!callback.alive()) return@runCatching
            handler.sendEmptyMessage(Callback.WHAT_CLOSE)
        }.onFailure { it.printStackTrace() }
    }

    fun path(context: Context): File {
        val cache: File = context.externalCacheDir ?: context.cacheDir
        val file = File(cache, "${NAMESPACE}${File.separator}circle.log")
        val parent = file.parentFile ?: return file
        if (parent.exists()) return file
        parent.mkdirs()
        return file
    }

    fun v(tag: String?, msg: String?, ex: Throwable? = null) = log("V", tag, msg, ex)

    fun i(tag: String?, msg: String?, ex: Throwable? = null) = log("I", tag, msg, ex)

    fun d(tag: String?, msg: String?, ex: Throwable? = null) = log("D", tag, msg, ex)

    fun w(tag: String?, msg: String?, ex: Throwable? = null) = log("W", tag, msg, ex)

    fun e(tag: String?, msg: String?, ex: Throwable? = null) = log("E", tag, msg, ex)

    private fun log(level: String, tag: String?, value: String?, ex: Throwable?) {
        if (tag.isNullOrEmpty() || value.isNullOrEmpty()) return
        if (!BuildConfig.DEBUG && !allowLevels.contains(level)) return
        kotlin.runCatching {
            if (!callback.alive()) return@runCatching
            val handler = Logcat.handler ?: return@runCatching
            val block = logsMap[level] ?: return@runCatching
            if (BuildConfig.DEBUG || level == "E") block(tag, value, ex)
            val fmt = logFmt ?: LogFormat()
            logFmt = fmt
            val msg = handler.obtainMessage(Callback.WHAT_SINK)
            msg.obj = fmt.of(level, tag, value, ex)
            handler.sendMessage(msg)
        }.onFailure { it.printStackTrace() }
    }

    private class Callback : Handler.Callback {
        private var thread: HandlerThread? = null

        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                WHAT_OPEN -> {
                    val path: String = (msg.obj as? String) ?: return true
                    if (path.isEmpty()) return true
                    thread = Thread.currentThread() as? HandlerThread
                    thread ?: return true
                    CircleJournal.open(path, 10 * 1024 * 1024L, 0.25f)
                }
                WHAT_SINK -> {
                    val value: String = (msg.obj as? String) ?: return true
                    if (value.isEmpty()) return true
                    CircleJournal.sink(value)
                }
                WHAT_CLOSE -> {
                    CircleJournal.close()
                    thread?.quit()
                    thread = null
                }
            }
            return true
        }

        fun alive(): Boolean = thread?.isAlive == true

        companion object {
            const val WHAT_OPEN = 1001
            const val WHAT_SINK = 1002
            const val WHAT_CLOSE = 1003
        }
    }
}