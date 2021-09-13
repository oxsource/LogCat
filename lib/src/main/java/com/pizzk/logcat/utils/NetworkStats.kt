package com.pizzk.logcat.utils

import android.content.Context
import android.net.*
import android.os.Build

internal object NetworkStats {

    enum class Transport {
        NONE,
        WIFI,
        CELLULAR,
        ETHERNET
    }

    fun check(context: Context?): Transport {
        context ?: return Transport.NONE
        val service = context.getSystemService(Context.CONNECTIVITY_SERVICE)
        val manager: ConnectivityManager = service as? ConnectivityManager ?: return Transport.NONE
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network: Network = manager.activeNetwork ?: return Transport.NONE
            val capabilities: NetworkCapabilities = manager.getNetworkCapabilities(network)
                ?: return Transport.NONE
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return Transport.WIFI
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return Transport.CELLULAR
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return Transport.ETHERNET
            Transport.NONE
        } else {
            val info: NetworkInfo = manager.activeNetworkInfo ?: return Transport.NONE
            val type: Int = info.type
            if (ConnectivityManager.TYPE_WIFI == type) return Transport.WIFI
            if (ConnectivityManager.TYPE_MOBILE == type) return Transport.CELLULAR
            if (ConnectivityManager.TYPE_ETHERNET == type) return Transport.ETHERNET
            Transport.NONE
        }
    }
}