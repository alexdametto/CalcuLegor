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

    private String UUID;

    public DialogListAdapter(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.adapter = new DeviceListArrayAdapter(c, new ArrayList<BluetoothDevice>());
        this.BTAdapter = BluetoothHelper.BTAdapter;
        adapter.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.list_dialog);

        ListView l = findViewById(R.id.lista);
        l.setAdapter(this.adapter);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        c.registerReceiver(bReciever, filter);
        BTAdapter.startDiscovery();

        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                c.unregisterReceiver(bReciever);
                BTAdapter.cancelDiscovery();
            }
        });
    }



    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                // Add it to our adapter

                Toast.makeText(adapter.getContext(), "OOOOOOOO", Toast.LENGTH_SHORT);
                adapter.add(device);
            }
        }
    };


    public String getSelected() {
        return adapter.getSelected();
    }
}
