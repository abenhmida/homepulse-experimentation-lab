package com.krizaldis.homepulse.command

data class SetBrightness(
    val brightness: Int
) {
    init {
        require(brightness in 0..100) {
            "Brightness must be between 0 and 100"
        }
    }
}