package com.hungama.fetch2.encryptiondecryption;

public class ScrambleKeyGenerator {

    public static int getKeySize() {
        return 0xFF; // this is the period of LSR defined below, found out
        // empirically
    }

    public static byte[] generateKey(int deviceId) {
        int _k = ((deviceId >>> 24) ^ (deviceId >>> 16) ^ (deviceId >>> 8) ^ deviceId) & 255;
        //Log.s("generateKey :::: " + _k);
        byte[] key = new byte[getKeySize()];

        for (int i = 0; i < key.length; ++i) {
            key[i] = (byte) _k;

            // LSR (8 bits) taps: 1,6,7,8 (from left to right)
            // See http://homepage.mac.com/afj/lfsr.html
            int input = (_k & 128) ^ ((_k & 4) << 5) ^ ((_k & 2) << 6)
                    ^ ((_k & 1) << 7);
            _k = ((_k >>> 1) | input) & 255;
        }

        return key;
    }
}
