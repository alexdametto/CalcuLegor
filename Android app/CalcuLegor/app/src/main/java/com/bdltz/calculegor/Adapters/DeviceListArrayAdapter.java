package com.bdltz.calculegor.Adapters;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bdltz.calculegor.R;

import java.util.List;

public class DeviceListArrayAdapter extends ArrayAdapter<BluetoothDevice> {


    private final List<BluetoothDevice> list;
    private final Activity context;

    private String selected = "";

    static class ViewHolder {
        protected TextView testo;
    }

    public DeviceListArrayAdapter(Activity context, List<BluetoothDevice> list) {
        super(context, R.layout.bt_device_row, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.bt_device_row, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.testo = (TextView) view.findViewById(R.id.testo);
            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.testo.setText(list.get(position).getName() + " (" + list.get(position).getAddress() + ")");

        if(list.get(position).getName() == "EV3")
            this.selected = list.get(position).getAddress();


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openConnection(list.get(position));
            }
        });

        return view;
    }

    public void openConnection(BluetoothDevice bt) {
        this.selected = bt.getAddress();
    }

    public String getSelected() {
        return this.selected;
    }
}
