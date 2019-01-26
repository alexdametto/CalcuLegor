package com.bdltz.calculegor.Adapters;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.bdltz.calculegor.Helpers.BluetoothHelper;
import com.bdltz.calculegor.R;

import java.util.ArrayList;

public class DialogListAdapter extends Dialog {

    private Activity c;
    private DeviceListArrayAdapter adapter;
    private BluetoothAdapter BTAdapter;

    private BluetoothDevice UUID;

    public DialogListAdapter(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.adapter = new DeviceListArrayAdapter(c, BluetoothHelper.getBondedDevices(), this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.list_dialog);

        ListView l = findViewById(R.id.lista);
        l.setAdapter(this.adapter);
    }


    public BluetoothDevice getSelected() {
        return adapter.getSelected();
    }
}
