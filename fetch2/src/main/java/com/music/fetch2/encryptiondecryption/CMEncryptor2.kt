package com.hungama.fetch2.encryptiondecryption

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.nio.charset.StandardCharsets
import kotlin.experimental.xor

class CMEncryptor2 {
    private var _key: ByteArray? = null
    private var _keyByteIndex = 0
    var usejni = false
    var isDecrypt = false

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    constructor(deviceID: String, usejni: Boolean, isDecrypt: Boolean) {
        this.isDecrypt = isDecrypt
        _key = ScrambleKeyGenerator.generateKey(deviceID.toInt())
        val str: String
        str = String(_key!!, StandardCharsets.UTF_8)
        this.usejni = usejni
        if (usejni) {
            try {
                if (isDecrypt) {
                    MyJNI.reset1()
                    Log.i(
                        "EncryptRunnable",
                        "Encrypt: TrackId: Reset1 :: isDecrypt:$isDecrypt"
                    )
                } else {
                    MyJNI.reset()
                    Log.i(
                        "EncryptRunnable",
                        "Encrypt: TrackId: Reset :: isDecrypt:$isDecrypt"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (e: Error) {
                e.printStackTrace()
            }
        }
    }

    // used to encrypt
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    constructor(deviceID: String) {
        _key = ScrambleKeyGenerator.generateKey(deviceID.toInt())
        val str: String
        str = String(_key!!, StandardCharsets.UTF_8)
        //Log.s("key generated :::::: " + str);
    }

    fun encrypt(bytes: ByteArray, off: Int, len: Int) {
        if (usejni) {
            try {
                MyJNI.decode(bytes, _key, len)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (e: Error) {
                e.printStackTrace()
            }
        }
        scramble(bytes, off, len)
    }

    fun decrypt(bytes: ByteArray, off: Int, len: Int) {
        try {
            MyJNI.decode1(bytes, _key, len)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: Error) {
            e.printStackTrace()
        }
        scramble(bytes, off, len)
    }

    private fun scramble(buffer: ByteArray, offset: Int, length: Int) {
        var temp: Int
        val keylength = _key!!.size
        var i: Int = offset
        while (i < offset + length) {
            buffer[i] = (buffer[i] xor _key!![_keyByteIndex]) as Byte
            if (_keyByteIndex + 1 >= keylength) _keyByteIndex = 0 else ++_keyByteIndex
            i++
        }
    }
}