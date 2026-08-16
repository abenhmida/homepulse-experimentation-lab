package com.krizaldis.homepulse.partitioning

data class KafkaPartitionKey(
    val value: String
) {
    companion object {
        fun forDevice(
            homeId: String,
            deviceId: String
        ): KafkaPartitionKey =
            KafkaPartitionKey("$homeId:$deviceId")
    }
}
