package com.pizzk.logcat.report

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.pizzk.logcat.Logcat
import com.pizzk.logcat.log.Logger
import com.pizzk.logcat.state.Defaults
import com.pizzk.logcat.state.States
import com.pizzk.logcat.utils.JsonUtils
import com.pizzk.logcat.utils.NetworkStats
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.jetbrains.anko.doAsync
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

internal object Reporter {
    private const val TAG = "Reporter"
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
        active.set(true)
        retryControl.reset()
        handler.post(checkCallback)
    }

    fun stopCheck() {
        if (!active.get()) return
        active.set(false)
        handler.removeCallbacks(checkCallback)
    }

    private fun intervalCheck(): Boolean {
        if (States.plan().id.isEmpty()) return false
        if (working.get()) return false
        //use expires control whether submit
        val expires = States.plan().expires
        val stamp = System.currentTimeMillis()
        if (!Defaults.crashed() && stamp - expires < 0) return false
        working.set(true)
        doAsync {
            runCatching { submit() }.onFailure { it.printStackTrace() }
            working.set(false)
        }
        return true
    }

    @Throws(Exception::class)
    private fun submit() {
        if (States.plan().id.isEmpty()) return
        Log.d(TAG, "Logcat Reporter try to submit.")
        val delegate = Logcat.config().planProvider
        val context = States.context() ?: return
        //network and data flow protect check
        val transport = NetworkStats.check(context)
        if (NetworkStats.Transport.NONE == transport) return
        if (States.plan().reportOnWifi && NetworkStats.Transport.WIFI != transport) return
        //collect log and meta file
        val fLog: File = Logger.path(context)
        val zipName = "${States.plan().id}-${States.plan().name}.zip"
        val fZip = File(fLog.absolutePath.replace(fLog.name, zipName))
        if (!fZip.exists() || fZip.length() <= 0) {
            if (!fLog.exists() || fLog.length() <= 0) return
            val metas: Map<String, Any> = delegate.metas(context)
            val metasText: String = JsonUtils.json(metas)
            val fMeta = File("${fLog.absolutePath}.meta")
            kotlin.runCatching { fMeta.bufferedWriter().use { it.write(metasText) } }
            val files: List<File> = listOf(fLog, fMeta)
            //zip files
            kotlin.runCatching { zips(fZip, files) }.onFailure { fZip.delete() }
            if (fMeta.exists()) fMeta.delete()
        }
        if (!fZip.exists() || fZip.length() <= 0) return
        kotlin.runCatching {
            val success = delegate.push(States.plan().id, fZip)
            if (!success) return@runCatching
            Log.d(TAG, "Logcat Reporter submit success")
            //reset states after push success
            Defaults.crashed(value = false)
            States.plan().reset()
            Defaults.savePlan()
            if (fLog.exists()) fLog.delete()
        }
        if (fZip.exists()) fZip.delete()
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