package com.krizaldis.homepulse.device

@JvmInline
value class HomeId(
    val value: String
){
    init {
        require(value.isNotBlank()) {
            "Home ID must not be blank"
        }
    }
}