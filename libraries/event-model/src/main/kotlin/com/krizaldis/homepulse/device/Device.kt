package com.krizaldis.homepulse.device

data class Device(
    val id: DeviceId,
    val homeId: HomeId,
    val type: DeviceType,
    val roomId: String,
    val capabilities: Set<DeviceCapability>
) {
    init {
        require(roomId.isNotBlank()) {
            "Room ID must not be blank"
        }
    }

    fun supports(capability: DeviceCapability): Boolean =
        capability in capabilities
}