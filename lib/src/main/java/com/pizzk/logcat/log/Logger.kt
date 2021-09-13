package com.pizzk.logcat.log

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import com.pizzk.logcat.state.States
import java.io.File

internal object Logger {
    private const val NAMESPACE = "roselle-logs"
    const val V = "V"
    const val I = "I"
    const val D = "D"
    const val W = "W"
    const val E = "E"

    //private props
    private val callback = Callback()
    private val convertor = Convertor()
    private var handler: Handler? = null
    private var flushFinish: () -> Unit = {}
    private var destroyFinish: () -> Unit = {}

    private val logMaps: Map<String, (String?, String?, Throwable?) -> Unit> = mapOf(
        Pair(V, { t, m, e -> Log.v(t, m, e) }),
        Pair(I, { t, m, e -> Log.i(t, m, e) }),
        Pair(D, { t, m, e -> Log.d(t, m, e) }),
        Pair(W, { t, m, e -> Log.w(t, m, e) }),
        Pair(E, { t, m, e -> Log.e(t, m, e) }),
    )

    private fun prepare(setup: Boolean): Handler? {
        if (!States.plan().loggable) return null
        if (null != handler) return handler
        if (!setup) return null
        val context: Context = States.context() ?: return null
        return kotlin.runCatching {
            val thread = HandlerThread(NAMESPACE)
            val handler = Handler(thread.looper, callback)
            thread.start()
            val msg = handler.obtainMessage(Callback.WHAT_SETUP)
            msg.obj = path(context).absolutePath
            handler.sendMessage(msg)
            Logger.handler = handler
            return@runCatching handler
        }.onFailure { it.printStackTrace() }.getOrNull()
    }

    fun flush(finish: () -> Unit) {
        val handler = prepare(setup = false) ?: return finish()
        kotlin.runCatching {
            if (!callback.alive()) return finish()
            flushFinish = finish
            handler.sendEmptyMessage(Callback.WHAT_FLUSH)
            return@runCatching
        }.onFailure {
            it.printStackTrace()
            finish()
        }
    }

    fun destroy(finish: () -> Unit) {
        val handler = prepare(setup = false) ?: return finish()
        kotlin.runCatching {
            if (!callback.alive()) return finish()
            destroyFinish = finish
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

    fun log(level: String, tag: String?, value: String?, ex: Throwable?) {
        if (tag.isNullOrEmpty() || value.isNullOrEmpty()) return
        logMaps[level]?.let { block -> block(tag, value, ex) }
        if (!States.plan().logLevels.contains(level)) return
        val context: Application = States.context() ?: return
        val handler = prepare(setup = true) ?: return
        kotlin.runCatching {
            if (!callback.alive()) return@runCatching
            val msg = handler.obtainMessage(Callback.WHAT_SINK)
            msg.obj = convertor.text(context, level, tag, value, ex)
            handler.sendMessage(msg)
        }.onFailure { it.printStackTrace() }
    }

    private class Callback : Handler.Callback {
        private var thread: HandlerThread? = null

        override fun handleMessage(msg: Message): Boolean {
            kotlin.runCatching {
                when (msg.what) {
                    WHAT_SETUP -> setup(msg)
                    WHAT_SINK -> sink(msg)
                    WHAT_FLUSH -> flush()
                    WHAT_DESTROY -> destroy()
                }
            }.onFailure { it.printStackTrace() }
            return true
        }

        private fun setup(msg: Message) {
            val path: String = (msg.obj as? String) ?: return
            if (path.isEmpty()) return
            thread = Thread.currentThread() as? HandlerThread
            thread ?: return
            Roselle.setup(path, 5 * 1024 * 1024L)
        }

        private fun sink(msg: Message) {
            val value: String = (msg.obj as? String) ?: return
            if (value.isEmpty()) return
            Roselle.sink(value, value.length)
        }

        private fun flush() {
            Roselle.flush()
            flushFinish()
        }

        private fun destroy() {
            Roselle.flush()
            destroyFinish()
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