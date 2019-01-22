package com.bdltz.calculegor;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.bdltz.calculegor.Helpers.BluetoothHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import Lego.Packet;

import static android.app.Activity.RESULT_OK;

public class FragmentCalcolatrice extends Fragment {
    View rootview;

    ImageButton voce, camera;
    EditText espressione;
    TextView connected;
    Button invia;

    SocketMonitor sm;

    private final static int REQ_CODE_SPEECH_INPUT = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater infilater, ViewGroup container, Bundle savedInstanceState)
    {
        rootview = infilater.inflate(R.layout.fragment_calcolatrice, container, false);
        setHasOptionsMenu(true);
        return rootview;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        voce = rootview.findViewById(R.id.mic);
        camera = rootview.findViewById(R.id.camera);
        espressione = rootview.findViewById(R.id.espressione);
        invia = rootview.findViewById(R.id.invia);
        connected = rootview.findViewById(R.id.conn);

        voce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                riconosciVoce();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                riconosciFoto();
            }
        });

        invia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickPulsante();
            }
        });

        sm = new SocketMonitor(connected);

        Thread t2 = new Thread(sm);
        t2.start();
    }

    @Override
    public void onDestroy() {
        try {
            BluetoothHelper.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void riconosciVoce(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
        }
    }

    private void riconosciFoto(){
        Intent myIntent = new Intent(rootview.getContext(), CameraActivity.class);
        rootview.getContext().startActivity(myIntent);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    espressione.setText(convertiVoce(result.get(0)));
                }
                break;
            }

        }
    }

    // RIVEDERE QUESTO METODO
    private String convertiVoce(String dettato){
        System.out.println(dettato);

        dettato = dettato.replace("più", "+");
        dettato = dettato.replace("piu", "+");
        dettato = dettato.replace("meno", "-");
        dettato = dettato.replace("per", "*");
        dettato = dettato.replace("diviso", "/");
        dettato = dettato.replace("fratto", "/");

        return dettato;
    }

    public void clickPulsante() {
        Button btn = rootview.findViewById(R.id.invia);
        btn.setEnabled(false);

        EditText e = rootview.findViewById(R.id.espressione);
        try {
            BluetoothHelper.send(new Packet(Packet.KEY_EXP, e.getText().toString()));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    private static class SocketMonitor implements Runnable {
        private TextView text;
        private boolean exe = true;

        public SocketMonitor(TextView text) {
            this.text = text;
        }

        public void terminate() {
            this.exe = false;
        }

        @Override
        public void run() {
            try {
                while(exe) {
                    final boolean conn = BluetoothHelper.isConnected();

                    text.post(new Runnable() {
                        @Override
                        public void run() {
                            if(conn)
                                text.setText("Connesso...");
                            else text.setText("Non connesso...");
                        }
                    });

                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
