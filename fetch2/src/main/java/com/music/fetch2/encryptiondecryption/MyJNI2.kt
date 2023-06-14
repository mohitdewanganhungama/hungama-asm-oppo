package com.hungama.fetch2.encryptiondecryption

object MyJNI2 {

    // Used to load the 'native-lib' library on application startup.
    init {
        System.loadLibrary("native-lib")
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun decode(input: ByteArray?, key: ByteArray?, length: Int): ByteArray?
    external fun reset()
    external fun reset1()
    external fun decode1(input: ByteArray?, key: ByteArray?, length: Int): ByteArray?
}