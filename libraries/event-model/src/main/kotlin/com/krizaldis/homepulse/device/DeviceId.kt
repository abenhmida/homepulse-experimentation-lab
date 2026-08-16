package com.krizaldis.homepulse.device

@JvmInline
value class DeviceId(
    val value: String
){
    init {
        require(value.isNotBlank()) {
            "Device ID must not be blank"
        }
    }
}