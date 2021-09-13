package com.pizzk.logcat.report

import com.pizzk.logcat.state.Plan
import java.io.File

open class SyncProvider {

    open fun fetch(ids: String, alias: String): Plan? = null

    open fun upload(file: File) = Unit
}