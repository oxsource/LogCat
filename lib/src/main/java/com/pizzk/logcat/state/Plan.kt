package com.pizzk.logcat.state

data class Plan(
    //计划ID(由日志系统自动生成)
    var id: String = "",
    //计划名称(由用户在前端管控台输入，必填字段)
    var name: String = "",
    //日志文件加密秘钥(由日志系统自动生成)
    var secret: String = "",
    //计划有效期(eg. 毫秒级时间戳，由前端选择，必填字段，到期后上报日志)
    var expires: Long = 0,
    //日志级别(V,I,D,W,E，由前端选择输入，默认全部)
    var logLevels: String = "",
    //是否在wifi环境下上报(true|false, 由用户在前端管控台输入，必填字段，默认true)
    var reportOnWifi: Boolean = false
) {

    fun of(v: Plan) {
        id = v.id
        name = v.name
        secret = v.secret
        expires = v.expires
        logLevels = v.logLevels
        reportOnWifi = v.reportOnWifi
    }

    fun reset() {
        id = ""
        name = ""
        secret = ""
        expires = 0
        logLevels = ""
        reportOnWifi = true
    }

    fun loggable(): Boolean {
        if (id.isEmpty()) return false
        return expires - System.currentTimeMillis() > 0
    }
}