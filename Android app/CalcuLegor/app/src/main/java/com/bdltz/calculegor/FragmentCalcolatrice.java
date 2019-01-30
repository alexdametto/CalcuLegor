package com.bdltz.calculegor;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.view.menu.ActionMenuItemView;
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
    // variabili locali
    View rootview;

    ImageButton voce, camera;
    EditText espressione;
    TextView connected;
    Button invia, btnConn;
    ActionMenuItemView battery;

    // thread che monitora il socket
    SocketMonitor sm;

    // codici per i permessi
    private final static int REQ_CODE_SPEECH_INPUT = 1;
    private final static int REQ_CODE_FOTO = 2;

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

        // inizializzo
        voce = rootview.findViewById(R.id.mic);
        camera = rootview.findViewById(R.id.camera);
        espressione = rootview.findViewById(R.id.espressione);
        invia = rootview.findViewById(R.id.invia);
        connected = rootview.findViewById(R.id.conn);
        btnConn = rootview.findViewById(R.id.connetti);

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

        btnConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnConn.getText().toString().equals(getText(R.string.key_connetti)))
                    connect();
                else disconnect();
            }
        });

        // creo thread
        sm = new SocketMonitor(connected, btnConn, invia, getActivity());

        Thread t2 = new Thread(sm);
        t2.start();

        // controllo i permessi se sono garantiti per nascondere il pulsante fotocamera
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            camera.setVisibility(View.INVISIBLE);
        }
    }

    // mi connetto
    public void connect() {
        BluetoothHelper.scegliDispositivo(getActivity());
    }

    // mi disconnetto
    public void disconnect() {
        try {
            BluetoothHelper.disconnect(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // avvio riconoscimento voce
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

    // avvio riconoscimento foto
    private void riconosciFoto(){
        Intent myIntent = new Intent(rootview.getContext(), CameraActivity.class);
        startActivityForResult(myIntent, REQ_CODE_FOTO);
    }


    // una volta chiusa un activity, guardo quale è in base al codice e faccio quello che devo fare
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    espressione.setText(convertiTesto(result.get(0)));
                }
                break;

            case REQ_CODE_FOTO:
                if (resultCode == RESULT_OK && null != data) {
                    String result=data.getStringExtra("result");
                    espressione.setText(convertiTesto(result));
                }
                break;
        }
    }

    // quando ottengo un input da voce, converto il testo
    private String convertiTesto(String dettato){
        System.out.println(dettato);

        dettato = dettato.replace("somma", "+");
        dettato = dettato.replace("più", "+");
        dettato = dettato.replace("piu", "+");
        dettato = dettato.replace("meno", "-");
        dettato = dettato.replace("sottrazione", "-");
        dettato = dettato.replace("per", "*");
        dettato = dettato.replace("moltiplicazione", "*");
        dettato = dettato.replace("diviso", "/");
        dettato = dettato.replace("divisione", "/");
        dettato = dettato.replace("fratto", "/");

        return dettato;
    }

    // quando clicca su invia
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

    // thread che monitora il socket per vedere se è connesso o no
    private static class SocketMonitor implements Runnable {
        private TextView text;
        private Button btn, btnInvia;
        private boolean exe = true;
        private Activity a;
        private ActionMenuItemView battery;

        public SocketMonitor(TextView text, Button btn, Button btnInvia, Activity a) {
            this.text = text;
            this.a = a;
            this.btn = btn;
            this.btnInvia = btnInvia;
        }

        public void terminate() {
            this.exe = false;
        }

        @Override
        public void run() {
            try {
                while(exe) {
                    final boolean conn = BluetoothHelper.isConnected();

                    // aggiorno grafica

                    battery = a.findViewById(R.id.battery);

                    if(battery != null) {
                        battery.post(new Runnable() {
                            @Override
                            public void run() {
                                if (conn) {
                                    battery.setVisibility(View.VISIBLE);
                                } else {
                                    battery.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }

                    text.post(new Runnable() {
                        @Override
                        public void run() {
                            if(conn) {
                                text.setText(a.getText(R.string.key_connesso));
                            }
                            else {
                                text.setText(a.getText(R.string.key_disconnesso));
                            }
                        }
                    });

                    btn.post(new Runnable() {
                        @Override
                        public void run() {
                            if(conn) {
                                btn.setText(a.getText(R.string.key_disconnetti));
                            }
                            else {
                                btn.setText(a.getText(R.string.key_connetti));
                            }
                        }
                    });

                    btnInvia.post(new Runnable() {
                        @Override
                        public void run() {
                            if(conn) {
                                btnInvia.setEnabled(true);
                            }
                            else {
                                btnInvia.setEnabled(false);
                            }
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
