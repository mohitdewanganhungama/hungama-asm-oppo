package com.hungama.fetch2.encryptiondecryption

object ScrambleKeyGenerator2 {
    // this is the period of LSR defined below, found out
    // empirically
    val keySize: Int
        get() = 0xFF // this is the period of LSR defined below, found out
    // empirically

    fun generateKey(deviceId: Int): ByteArray {
        var _k = deviceId ushr 24 xor (deviceId ushr 16) xor (deviceId ushr 8) xor deviceId and 255
        //Log.s("generateKey :::: " + _k);
        val key = ByteArray(keySize)
        for (i in key.indices) {
            key[i] = _k.toByte()

            // LSR (8 bits) taps: 1,6,7,8 (from left to right)
            // See http://homepage.mac.com/afj/lfsr.html
            val input = (_k and 128 xor (_k and 4 shl 5) xor (_k and 2 shl 6)
                    xor (_k and 1 shl 7))
            _k = _k ushr 1 or input and 255
        }
        return key
    }
}