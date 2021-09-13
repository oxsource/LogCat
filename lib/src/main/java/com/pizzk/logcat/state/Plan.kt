package com.pizzk.logcat.state

data class Plan(
    var loggable: Boolean = false,
    var logLevels: Set<String> = emptySet(),
    var reportable: Boolean = false,
    var reportWhileWifi: Boolean = true
) {
    fun of(v: Plan) {
        loggable = v.loggable
        logLevels = v.logLevels
        reportable = v.reportable
        reportWhileWifi = v.reportWhileWifi
    }

    fun reset() {
        loggable = false
        logLevels = emptySet()
        reportable = false
        reportWhileWifi = true
    }
}