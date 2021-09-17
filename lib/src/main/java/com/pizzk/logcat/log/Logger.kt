package com.pizzk.logcat.log

import android.content.Context
import android.util.Log
import com.pizzk.logcat.BuildConfig
import com.pizzk.logcat.state.States
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal object Logger {
    private const val NAMESPACE = "roselle-logs"

    //use internal storage, real max use size is three times of <MAX_CACHE_SIZE>
    private const val MAX_CACHE_SIZE = 5 * 1024 * 1024L
    const val V = "V"
    const val I = "I"
    const val D = "D"
    const val W = "W"
    const val E = "E"

    private val convertor = Convertor()
    private val logMaps: Map<String, (String?, String?, Throwable?) -> Unit> = mapOf(
        Pair(V, { t, m, e -> Log.v(t, m, e) }),
        Pair(I, { t, m, e -> Log.i(t, m, e) }),
        Pair(D, { t, m, e -> Log.d(t, m, e) }),
        Pair(W, { t, m, e -> Log.w(t, m, e) }),
        Pair(E, { t, m, e -> Log.e(t, m, e) }),
    )
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val roselle: AtomicBoolean = AtomicBoolean()

    internal fun setup() {
        if (roselle.get()) return
        val context: Context = States.context() ?: return
        executor.submit submit@{
            kotlin.runCatching {
                val path = path(context).absolutePath
                Roselle.setup(path, MAX_CACHE_SIZE)
                roselle.set(true)
            }.onFailure { it.printStackTrace() }
        }
    }

    internal fun flush(finish: () -> Unit = {}) {
        if (!roselle.get()) return finish()
        executor.submit {
            kotlin.runCatching { Roselle.flush() }
            finish()
        }
    }

    internal fun path(context: Context): File {
        val cache: File = context.cacheDir
        val file = File(cache, "$NAMESPACE${File.separator}roselle.log")
        val parent = file.parentFile ?: return file
        if (parent.exists()) return file
        parent.mkdirs()
        return file
    }

    internal fun log(level: String, tag: String?, value: String?, ex: Throwable?) {
        if (tag.isNullOrEmpty() || value.isNullOrEmpty()) return
        if (BuildConfig.DEBUG || States.plan().logLevels.contains(level)) {
            logMaps[level]?.invoke(tag, value, ex)
        }
        if (!roselle.get() || !States.plan().loggable()) return
        executor.submit {
            kotlin.runCatching {
                val block = convertor.text(level, tag, value, ex)
                Roselle.sink(block, block.length)
            }.onFailure { it.printStackTrace() }
        }
    }
}