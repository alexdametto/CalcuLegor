package com.bdltz.calculegor;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Camera;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class FragmentCalcolatrice extends Fragment {
    View rootview;

    ImageButton voce, camera;
    EditText espressione;
    Button invia;

    private final static int REQ_CODE_SPEECH_INPUT = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater infilater, ViewGroup container, Bundle savedInstanceState)
    {
        rootview = infilater.inflate(R.layout.fragment_calcolatrice, container, false);
        return rootview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        voce = (ImageButton)rootview.findViewById(R.id.mic);
        camera = (ImageButton)rootview.findViewById(R.id.camera);
        espressione = (EditText)rootview.findViewById(R.id.espressione);
        invia = (Button)rootview.findViewById(R.id.invia);

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


    }

    private void riconosciVoce(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

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
}
