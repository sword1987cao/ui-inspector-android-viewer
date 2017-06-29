package com.raybritton.uiinspectorserver

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import timber.log.Timber
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException

object Utils {
    fun getIpAddress(ctx: Context): InetAddress? {
        try {
            val wifiManager = ctx.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val addressNum = wifiManager.connectionInfo.ipAddress
            val bytes = BigInteger.valueOf(addressNum.toLong()).toByteArray()
            bytes.reverse()
            return InetAddress.getByAddress(bytes)
        } catch (e: UnknownHostException) {
            Timber.e(e, "get ip address")
            return null
        }

    }
}
