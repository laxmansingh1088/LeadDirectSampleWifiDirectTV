package com.example.leadp2pdirect.p2p

data class MyDeviceInfoForQrCode(
    val deviceName: String?,
    val deviceId: String?,
    val primaryDeviceType: String?,
    val secondaryDeviceType: String?,
    val status: Int
)
