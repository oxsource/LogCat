package com.pizzk.logcat.log

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.pizzk.logcat.BuildConfig
import com.pizzk.logcat.Logcat
import com.pizzk.logcat.shell.AdbShell
import com.pizzk.logcat.state.States
import com.pizzk.logcat.utils.JsonUtils
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

internal object Logger {
    private const val TAG = "Roselle.Logger"
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
        val dir: File = context.filesDir
        val file = File(dir, "$NAMESPACE${File.separator}roselle.log")
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

    @WorkerThread
    internal fun dump(): File? {
        if (States.plan().id.isEmpty()) return null
        val context = States.context() ?: return null
        val delegate = Logcat.config().planProvider
        //collect log and meta file
        val fLog: File = path(context)
        val uuid: () -> String = uuid@{
            val limit = 8
            val value = UUID.randomUUID().toString().replace("-", "")
            return@uuid if (value.length > limit) value.substring(0, limit) else value
        }
        val names = arrayOf(States.plan().name, States.plan().id, uuid())
        val zipName = "${names.joinToString(separator = "_")}.zip"
        val fZip = File(fLog.absolutePath.replace(fLog.name, zipName))
        if (fZip.exists() && fZip.length() > 0) return fZip
        //
        val metas: Map<String, Any> = delegate.metas(context)
        val metasText: String = JsonUtils.json(metas)
        val fMeta = File("${fLog.absolutePath}.meta")
        kotlin.runCatching { fMeta.bufferedWriter().use { it.write(metasText) } }
        //
        val fSLog = File("${fLog.absolutePath}.slog")
        AdbShell.nimble("logcat -df ${fSLog.absolutePath}")
        //zip files
        val files: List<File> = listOf(fLog, fMeta, fSLog).filter(File::exists)
        kotlin.runCatching { zips(fZip, files) }.onFailure {
            fZip.delete()
            Log.e(TAG, "dump file exp: ${it.message}")
            it.printStackTrace()
        }
        arrayOf(fMeta, fSLog).filter(File::exists).forEach(File::delete)
        return if (fZip.exists()) fZip else null
    }

    private fun zips(zipFile: File, files: List<File>) {
        if (zipFile.exists()) zipFile.delete()
        val secret = States.plan().secret
        val params = ZipParameters()
        params.compressionMethod = CompressionMethod.DEFLATE
        params.compressionLevel = CompressionLevel.NORMAL
        params.encryptionMethod = EncryptionMethod.ZIP_STANDARD
        params.isEncryptFiles = secret.isNotEmpty()
        val zFile = ZipFile(zipFile, secret.toCharArray())
        zFile.isRunInThread = false
        zFile.addFiles(files, params)
    }
}