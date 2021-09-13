package com.pizzk.logcat.report

import java.io.File

class SyncProvider {

    fun fetch(ids: String, alias: String): Plan? = null

    fun upload(file: File) = Unit

    data class Plan(
        var enable: Boolean = false,
        var levels: List<String> = emptyList(),
        var upload: Boolean = false,
        var wifi: Boolean = true
    )
}