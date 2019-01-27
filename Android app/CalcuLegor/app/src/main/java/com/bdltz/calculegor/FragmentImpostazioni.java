package com.bdltz.calculegor;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.bdltz.calculegor.Helpers.Salvataggi;

public class FragmentImpostazioni extends Fragment {
    View rootview;

    Switch audio, vibrazione, click;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater infilater, ViewGroup container, Bundle savedInstanceState)
    {
        rootview = infilater.inflate(R.layout.fragment_impostazioni, container, false);
        return rootview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        audio = rootview.findViewById(R.id.audio);
        vibrazione = rootview.findViewById(R.id.vibrazione);
        click = rootview.findViewById(R.id.clickProcedere);

        loadSettings();

        audio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Salvataggi.setAudio(getActivity(), isChecked);

                Toast.makeText(getActivity(), getString(R.string.key_connect_and_disconnect), Toast.LENGTH_LONG).show();
            }
        });

        vibrazione.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Salvataggi.setVibrazione(getActivity(), isChecked);

                Toast.makeText(getActivity(), getString(R.string.key_connect_and_disconnect), Toast.LENGTH_LONG).show();
            }
        });

        click.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Salvataggi.setClickProcedere(getActivity(), isChecked);

                Toast.makeText(getActivity(), getString(R.string.key_connect_and_disconnect), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadSettings() {
        audio.setChecked(Salvataggi.getAudio(getActivity()));
        vibrazione.setChecked(Salvataggi.getVibrazione(getActivity()));
        click.setChecked(Salvataggi.getClickProcedere(getActivity()));
    }
}
