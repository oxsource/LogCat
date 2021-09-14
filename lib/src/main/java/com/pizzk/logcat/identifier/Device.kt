package com.pizzk.logcat.identifier

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.pizzk.logcat.shell.AdbShell
import com.pizzk.logcat.state.States
import java.math.RoundingMode
import java.text.DecimalFormat

internal data class Device(
    val model: String = Build.MODEL,
    val osInfo: String = Build.VERSION.RELEASE,
    val sdkInt: Int = Build.VERSION.SDK_INT,
    val cpu: String = "${Build.HARDWARE.uppercase()}-${Build.BOARD.uppercase()}",
    val version: String = AdbShell.nimble("getprop sys.build.display.id"),
    val kernel: String = AdbShell.nimble("getprop ro.build.kernel.id"),
    val baseBand: String = AdbShell.nimble("getprop gsm.version.baseband"),
    val netHostname: String = AdbShell.nimble("getprop gsm.net.hostname"),
) {

    internal data class Memory(
        var storageTotal: String = "",
        var storageFree: String = "",
        var memTotal: String = "",
        var memFree: String = "",
    ) {

        private val hsFormat: DecimalFormat = DecimalFormat("#.0")

        init {
            hsFormat.roundingMode = RoundingMode.HALF_UP
            val sf = StatFs(Environment.getDataDirectory().path)
            storageTotal = hs(sf.blockCountLong * sf.blockSizeLong)
            storageFree = hs(sf.availableBytes)
            val obj = States.context()?.getSystemService(Context.ACTIVITY_SERVICE)
            val am = obj as? ActivityManager
            val info = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(info)
            memTotal = hs(info.totalMem)
            memFree = hs(info.availMem)
        }

        private fun hs(size: Long): String {
            if (size <= 0) return "0B"
            val radix = 1000
            if (size <= radix) return "${size}B"
            if (size < radix * radix) return "${hsFormat.format(size / radix)}KB"
            if (size < radix * radix * radix) return "${hsFormat.format(size / radix / radix)}MB"
            return "${hsFormat.format(size / radix / radix / radix)}GB"
        }
    }
}