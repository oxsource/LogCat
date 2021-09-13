package com.pizzk.logcat.identifier

internal object Identifier {
    private var alias: String = ""
    private var ids: String = ""

    fun setAlias(value: String) {
        alias = value
    }

    fun getAlias() = alias

    fun getIds(): String = ids
}