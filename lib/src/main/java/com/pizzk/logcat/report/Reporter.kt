package com.pizzk.logcat.report

import com.pizzk.logcat.identifier.Identifier
import com.pizzk.logcat.log.Logger
import com.pizzk.logcat.state.Defaults
import com.pizzk.logcat.state.States
import com.pizzk.logcat.utils.NetworkStats
import java.io.BufferedInputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object Reporter {
    private var metaProvider: MetaProvider? = null
    private var syncProvider: SyncProvider? = null

    fun setup(meta: MetaProvider, sync: SyncProvider) {
        this.metaProvider = meta
        this.syncProvider = sync
    }

    fun fetch(): Reporter {
        val ids = Identifier.getIds()
        val alias = Identifier.getAlias()
        val value = syncProvider?.fetch(ids, alias) ?: return this
        States.plan().of(value)
        Defaults.savePlan()
        return this
    }

    fun submit(): Reporter {
        if (!States.plan().reportable) return this
        val syncProvider = syncProvider ?: return this
        val context = States.context() ?: return this
        //network and data flow protect check
        val transport = NetworkStats.check(context)
        if (NetworkStats.Transport.NONE == transport) return this
        if (States.plan().reportWhileWifi && NetworkStats.Transport.WIFI != transport) return this
        //collect log and meta file
        val fLog: File = Logger.path(context)
        val fZip = File("${fLog.absolutePath}.zip")
        if (!fZip.exists() || fZip.length() <= 0) {
            if (!fLog.exists() || fLog.length() <= 0) return this
            val metas: String = metaProvider?.provide(context) ?: ""
            val fMeta = File("${fLog.absolutePath}.meta")
            kotlin.runCatching { fMeta.bufferedWriter().use { it.write(metas) } }
            val files: List<File> = listOf(fLog, fMeta)
            //zip files
            kotlin.runCatching { zips(fZip, files) }.onFailure { fZip.delete() }
            fMeta.delete()
        }
        if (!fZip.exists() || fZip.length() <= 0) return this
        kotlin.runCatching { syncProvider.upload(fZip) }
        if (fZip.exists()) fZip.delete()
        return this
    }

    private fun zips(zipFile: File, files: List<File>) {
        if (zipFile.exists()) zipFile.delete()
        ZipOutputStream(zipFile.outputStream()).use { outs ->
            files.forEach { file ->
                val entry = ZipEntry(file.name)
                val ins: BufferedInputStream = file.inputStream().buffered()
                outs.putNextEntry(entry)
                ins.copyTo(outs)
            }
            outs.closeEntry()
        }
    }
}