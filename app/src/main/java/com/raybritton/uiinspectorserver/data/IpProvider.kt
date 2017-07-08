package com.raybritton.uiinspectorserver.data

import android.content.Context
import android.net.wifi.WifiManager
import java.math.BigInteger
import java.net.InetAddress

class IpProvider(val ctx: Context) {
    fun getIpAddress(): InetAddress {
        val wifiManager = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val addressNum = wifiManager.connectionInfo.ipAddress
        val bytes = BigInteger.valueOf(addressNum.toLong()).toByteArray()
        bytes.reverse()
        return InetAddress.getByAddress(bytes)
    }
}
