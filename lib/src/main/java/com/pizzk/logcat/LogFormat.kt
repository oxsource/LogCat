package com.pizzk.logcat

import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

open class LogFormat {
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss", Locale.SIMPLIFIED_CHINESE)

    open fun of(level: String, tag: String, value: String, ex: Throwable?): String {
        val time = sdf.format(Date())
        val pid = android.os.Process.myPid()
        val stack: String = ex?.let {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            it.printStackTrace(pw)
            return@let "$sw\n"
        } ?: ""
        return "$time ${level}/${tag}(${pid}): $value\n$stack"
    }
}