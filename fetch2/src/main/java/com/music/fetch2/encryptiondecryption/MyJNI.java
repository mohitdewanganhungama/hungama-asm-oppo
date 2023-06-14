package com.hungama.fetch2.encryptiondecryption;

public class MyJNI {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public static native byte[] decode(byte[] input,byte[] key, int length);
    public static native void reset();
    public static native void reset1();
    public static native byte[] decode1(byte[] input,byte[] key, int length);
}
