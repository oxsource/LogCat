package com.pizzk.logcat.log

import android.app.Application
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

internal class Convertor {
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss", Locale.SIMPLIFIED_CHINESE)

    fun text(
        app: Application,
        level: String,
        tag: String,
        value: String,
        ex: Throwable?
    ): String {
        //eg. 2021-09-10 21:33:59.986 5680-5809/com.pizzk.logcat.app D/OpenGLRenderer: Swap behavior 0
        val time = sdf.format(Date())
        val pid = android.os.Process.myPid()
        val tid = android.os.Process.myTid()
        val process: String = app.applicationInfo.processName
        val stack: String = ex?.let {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            it.printStackTrace(pw)
            val outs = sw.toString().trim()
            return@let "$outs\n"
        } ?: ""
        return "$time $pid-$tid/$process ${level}/${tag}: $value\n$stack"
    }
}