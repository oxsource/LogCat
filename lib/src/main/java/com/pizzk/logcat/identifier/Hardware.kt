package com.pizzk.logcat.identifier

data class Hardware(
    /**
     * 常规信息
     * phoneModel-手机型号
     * osInfo-系统信息
     * sdkInt-SDK版本
     */
    var phoneModel: String = "",
    var osInfo: String = "",
    var sdkInt: Int = 0,

    /**
     * 内存信息:
     * max-最大内存
     * total-已申请内存
     * free-可用内存
     * usage-使用内存
     */
    var memMax: Long = 0,
    var memTotal: Long = 0,
    var memFree: Long = 0,
    var memUsage: Long = 0,
) {

    init {
        /**获取基础信息*/
        phoneModel = android.os.Build.MODEL
        osInfo = android.os.Build.VERSION.RELEASE
        sdkInt = android.os.Build.VERSION.SDK_INT
        /**内存信息*/
        val runtime: Runtime = Runtime.getRuntime()
        memMax = runtime.maxMemory()
        memTotal = runtime.totalMemory()
        memFree = runtime.freeMemory()
        memUsage = runtime.totalMemory() - runtime.freeMemory()
    }
}