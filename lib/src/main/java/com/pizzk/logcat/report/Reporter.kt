package com.pizzk.logcat.report

import android.app.Application
import com.pizzk.logcat.identifier.Identifier
import com.pizzk.logcat.log.Logger
import com.pizzk.logcat.network.NetworkStats
import java.io.BufferedInputStream
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object Reporter {
    private var context: Application? = null
    private var metaProvider: MetaProvider? = null
    private var syncProvider: SyncProvider? = null
    private val plan: SyncProvider.Plan = SyncProvider.Plan()
    private val threadPool: ExecutorService = Executors.newFixedThreadPool(1)

    fun setup(context: Application, meta: MetaProvider, sync: SyncProvider) {
        this.context = context
        this.metaProvider = meta
        this.syncProvider = sync
    }

    fun fetch() {
        threadPool.execute {
            val ids = Identifier.getIds()
            val alias = Identifier.getAlias()
            val value = syncProvider?.fetch(ids, alias) ?: return@execute
            plan.enable = value.enable
            plan.levels = value.levels
            plan.upload = value.upload
            plan.wifi = value.wifi
            //caches
        }
    }

    fun plan(): SyncProvider.Plan = plan

    fun submit() {
        val syncProvider = syncProvider ?: return
        val context = this.context ?: return
        val transport = NetworkStats.check(context)
        //网络及流量保护检查
        if (NetworkStats.Transport.NONE == transport) return
        if (plan().wifi && NetworkStats.Transport.WIFI != transport) return
        //
        val file1: File = Logger.path(context)
        if (!file1.exists() || file1.length() <= 0) return
        val metas: String = metaProvider?.provide(context) ?: ""
        val file2 = File("${file1.absolutePath}.meta")
        kotlin.runCatching { file2.bufferedWriter().use { it.write(metas) } }
        val files: List<File> = listOf(file1, file2)
        //zip files
        val zip: File? = kotlin.runCatching {
            val zipFile = File("${file1.absolutePath}.zip")
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
            return@runCatching zipFile
        }.getOrNull()
        file2.delete()
        zip ?: return
        kotlin.runCatching { syncProvider.upload(zip) }
        zip.delete()
    }
}