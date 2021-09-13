package com.pizzk.logcat.app.provider

import com.pizzk.logcat.report.MetaProvider

class LogMetaProvider : MetaProvider() {

    override fun hook(map: MutableMap<String, Any>) {
        map["modified-by"] = "Mocks"
    }
}