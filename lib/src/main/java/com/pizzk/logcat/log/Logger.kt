package com.pizzk.logcat.log

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import com.pizzk.logcat.BuildConfig
import java.io.File

object Logger {
    private const val NAMESPACE = "roselle-logs"
    private var application: Application? = null
    private val callback = Callback()
    private var handler: Handler? = null
    private var logFmt: PrintFormat? = null
    private val allowLevels: MutableList<String> = listOf("W", "E").toMutableList()
    private var callbackDestroy: () -> Unit = {}

    private val logsMap: Map<String, (String?, String?, Throwable?) -> Unit> = mapOf(
        Pair("V", { t, m, e -> Log.v(t, m, e) }),
        Pair("I", { t, m, e -> Log.i(t, m, e) }),
        Pair("D", { t, m, e -> Log.d(t, m, e) }),
        Pair("W", { t, m, e -> Log.w(t, m, e) }),
        Pair("E", { t, m, e -> Log.e(t, m, e) }),
    )

    fun setup(app: Application, fmt: PrintFormat = PrintFormat()) {
        application = app
        logFmt = fmt
        kotlin.runCatching {
            val thread = HandlerThread(NAMESPACE)
            thread.start()
            val handler = Handler(thread.looper, callback)
            val msg = handler.obtainMessage(Callback.WHAT_SETUP)
            msg.obj = path(app.applicationContext).absolutePath
            handler.sendMessage(msg)
            Logger.handler = handler
        }.onFailure { it.printStackTrace() }
    }

    fun setAllowLevels(vararg levels: String) {
        allowLevels.clear()
        allowLevels.addAll(levels.toList())
    }

    fun flush() {
        val handler = handler ?: return
        kotlin.runCatching {
            if (!callback.alive()) return@runCatching
            handler.sendEmptyMessage(Callback.WHAT_FLUSH)
        }.onFailure { it.printStackTrace() }
    }

    fun destroy(finish: () -> Unit) {
        val handler = handler ?: return finish()
        kotlin.runCatching {
            if (!callback.alive()) return@runCatching finish()
            callbackDestroy = finish
            handler.sendEmptyMessage(Callback.WHAT_DESTROY)
            return@runCatching
        }.onFailure {
            it.printStackTrace()
            finish()
        }
    }

    fun path(context: Context): File {
        val cache: File = context.externalCacheDir ?: context.cacheDir
        val file = File(cache, "$NAMESPACE${File.separator}roselle.log")
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
        val app = application ?: return
        if (tag.isNullOrEmpty() || value.isNullOrEmpty()) return
        if (!BuildConfig.DEBUG && !allowLevels.contains(level)) return
        kotlin.runCatching {
            if (!callback.alive()) return@runCatching
            val handler = handler ?: return@runCatching
            val block = logsMap[level] ?: return@runCatching
            if (BuildConfig.DEBUG || level == "E") block(tag, value, ex)
            val fmt = logFmt ?: PrintFormat()
            logFmt = fmt
            val msg = handler.obtainMessage(Callback.WHAT_SINK)
            msg.obj = fmt.of(app, level, tag, value, ex)
            handler.sendMessage(msg)
        }.onFailure { it.printStackTrace() }
    }

    private class Callback : Handler.Callback {
        private var thread: HandlerThread? = null

        override fun handleMessage(msg: Message): Boolean {
            kotlin.runCatching {
                when (msg.what) {
                    WHAT_SETUP -> onSetup(msg)
                    WHAT_SINK -> onSink(msg)
                    WHAT_FLUSH -> Roselle.flush()
                    WHAT_DESTROY -> onDestroy()
                }
            }.onFailure { it.printStackTrace() }
            return true
        }

        private fun onSetup(msg: Message) {
            val path: String = (msg.obj as? String) ?: return
            if (path.isEmpty()) return
            thread = Thread.currentThread() as? HandlerThread
            thread ?: return
            Roselle.setup(path, 5 * 1024 * 1024L)
        }

        private fun onSink(msg: Message) {
            val value: String = (msg.obj as? String) ?: return
            if (value.isEmpty()) return
            Roselle.sink(value, value.length)
        }

        private fun onDestroy() {
            Roselle.flush()
            callbackDestroy()
        }

        fun alive(): Boolean = thread?.isAlive == true

        companion object {
            const val WHAT_SETUP = 1001
            const val WHAT_SINK = 1002
            const val WHAT_FLUSH = 1003
            const val WHAT_DESTROY = 1004
        }
    }
}