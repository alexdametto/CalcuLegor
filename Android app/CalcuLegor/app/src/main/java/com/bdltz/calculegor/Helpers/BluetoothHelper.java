package com.bdltz.calculegor.Helpers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import Lego.Packet;

public class BluetoothHelper {

    public static BluetoothAdapter BTAdapter;
    private static BluetoothSocket mSocket;
    private static ObjectOutputStream outputStream;

    public static void connect(BluetoothDevice device) throws IOException {
        if(device == null)
            return;

        ParcelUuid[] uuids = device.getUuids();
        mSocket =  device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
        mSocket.connect();

        outputStream = new ObjectOutputStream(mSocket.getOutputStream());
    }

    public static void send(Packet packet) throws IOException {
        outputStream.writeObject(packet);
    }
}
