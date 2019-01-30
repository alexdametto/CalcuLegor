package com.bdltz.calculegor;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    // variabili locali per gestire la fotocamera
    private CameraSource mCameraSource;
    private TextRecognizer mTextRecognizer;
    private SurfaceView mSurfaceView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // inizializzazione
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mTextView = (TextView) findViewById(R.id.textView);

        // controllo, anche se non servirebbe perchè il pulsante è nascosto in caso di non accettazione dei permessi
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startTextRecognizer();
        } else {
            // errore
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.key_error))
                    .setMessage(getText(R.string.key_qualcosa_storto))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_CANCELED, returnIntent);
                            finish();
                        }
                    })
                    .setIcon(R.drawable.warning)
                    .show();
        }

        // inizializzo
        ImageButton btn = findViewById(R.id.foto);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String testo = mTextView.getText().toString();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result",testo);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // rilascio la fotocamera
        mCameraSource.release();
    }

    // inizio il servizio di conversione OCR
    private void startTextRecognizer() {
        mTextRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!mTextRecognizer.isOperational()) {
            // errore se non funziona
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.key_error))
                    .setMessage(getText(R.string.key_qualcosa_storto))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_CANCELED, returnIntent);
                            finish();
                        }
                    })
                    .setIcon(R.drawable.warning)
                    .show();
        } else {
            // inizio
            mCameraSource = new CameraSource.Builder(getApplicationContext(), mTextRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(15.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    // controllo ancora, anche se non serve...
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            // inizio fotocamera
                            mCameraSource.start(mSurfaceView.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // qualcosa è andato storto....
                        new AlertDialog.Builder(CameraActivity.this)
                                .setTitle(getText(R.string.key_error))
                                .setMessage(getText(R.string.key_qualcosa_storto))
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent returnIntent = new Intent();
                                        setResult(Activity.RESULT_CANCELED, returnIntent);
                                        finish();
                                    }
                                })
                                .setIcon(R.drawable.warning)
                                .show();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();// fermo...
                }
            });

            // processo il testo
            mTextRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    SparseArray<TextBlock> items = detections.getDetectedItems();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < items.size(); ++i) {
                        TextBlock item = items.valueAt(i);
                        if (item != null && item.getValue() != null) {
                            stringBuilder.append(item.getValue() + " ");
                        }
                    }

                    final String fullText = stringBuilder.toString();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            // setto il testo
                            mTextView.setText(fullText);
                        }
                    });

                }
            });
        }
    }
}
