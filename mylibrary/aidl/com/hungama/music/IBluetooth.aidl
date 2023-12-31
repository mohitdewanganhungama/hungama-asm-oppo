// IBluetooth.aidl
package com.hungama.music;

// Declare any non-default types here with import statements

interface IBluetooth {
    /**
     * System private API for Bluetooth service
     */
    String getRemoteAlias(in String address);
      boolean setRemoteAlias(in String address, in String name);
}