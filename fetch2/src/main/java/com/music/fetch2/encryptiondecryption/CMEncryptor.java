package com.hungama.fetch2.encryptiondecryption;

import android.util.Log;

import java.nio.charset.StandardCharsets;

public class CMEncryptor {
    private byte[] _key = null;
    private int _keyByteIndex = 0;
    boolean usejni = false;
    boolean isDecrypt = false;
    public CMEncryptor(String deviceID,boolean usejni, boolean isDecrypt) {
        this.isDecrypt = isDecrypt;
        _key = ScrambleKeyGenerator.generateKey(Integer.parseInt(deviceID));
        String str;
        str = new String(_key, StandardCharsets.UTF_8);
        this.usejni = usejni;
        if (usejni) {
            try {
                if (isDecrypt) {
                    MyJNI.reset1();
                    Log.i("EncryptRunnable", "Encrypt: TrackId: Reset1 :: isDecrypt:" + isDecrypt);
                } else {
                    MyJNI.reset();
                    Log.i("EncryptRunnable", "Encrypt: TrackId: Reset :: isDecrypt:" + isDecrypt);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error e) {
                e.printStackTrace();
            }
        }
    }

    // used to encrypt
    public CMEncryptor(String deviceID) {
        _key = ScrambleKeyGenerator.generateKey(Integer.parseInt(deviceID));
        String str;
        str = new String(_key, StandardCharsets.UTF_8);
        //Log.s("key generated :::::: " + str);


    }

    public void encrypt(byte[] bytes, int off, int len) {
        if (usejni) {
            //Log.d("encripttt", "true1");
            try {
                MyJNI.decode(bytes, _key, len);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error e) {
                e.printStackTrace();
            }
        }
        //Log.d("encripttt", "true2");
        scramble(bytes, off, len);
    }

    public void decrypt(byte[] bytes, int off, int len) {
        if (usejni) {
            //Log.d("encripttt", "true3");
            try {
                MyJNI.decode1(bytes, _key, len);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error e) {
                e.printStackTrace();
            }
        }
        //Log.d("encripttt", "true4");
        scramble(bytes, off, len);
    }



    private void scramble(byte[] buffer, int offset, int length) {
        int temp,i, keylength = _key.length;
        for (i = offset; i < offset + length; i++) {
            buffer[i] = (byte) (buffer[i] ^ _key[_keyByteIndex]);
            if (_keyByteIndex + 1 >= keylength)
                _keyByteIndex = 0;
            else
                ++_keyByteIndex;
        }
    }
}
