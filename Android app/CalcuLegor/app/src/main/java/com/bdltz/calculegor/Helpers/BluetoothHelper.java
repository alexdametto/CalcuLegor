package com.bdltz.calculegor.Helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.Vibrator;
import android.support.v7.view.menu.ActionMenuItemView;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bdltz.calculegor.Adapters.DialogListAdapter;
import com.bdltz.calculegor.R;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import Lego.Packet;

public class BluetoothHelper {
    // variabili di connessione, stream
    private static BluetoothAdapter BTAdapter;
    private static BluetoothSocket mSocket;
    private static ObjectOutputStream outputStream;
    private static ObjectInputStream inputStream;

    // thread vari
    private static ReadEvents readEvents;
    private static Thread read;

    // booleana di connesso o no
    private static boolean connesso = false;

    // si connette a device
    public static void connect(BluetoothDevice device, Activity a) throws IOException {
        if(device == null)
            return;

        ParcelUuid[] uuids = device.getUuids();
        mSocket =  device.createRfcommSocketToServiceRecord(uuids[0].getUuid());

        // apre il socket
        mSocket.connect();

        // si è connesso
        connesso = true;

        // apro gli stream
        outputStream = new ObjectOutputStream(mSocket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(mSocket.getInputStream());

        // apri thread vari
        readEvents = new ReadEvents(a);
        read = new Thread(readEvents);
        read.start();

        // invio le impostazioni
        String settings = Salvataggi.getAudio(a) + ";" + Salvataggi.getClickProcedere(a);
        send(new Packet(Packet.KEY_IMPOSTAZIONI, settings));
    }

    // metodo per inviare un pacchetto
    public static void send(Packet packet) throws IOException {
        if(connesso) {
            outputStream.writeObject(packet);
            outputStream.flush();
        }
    }

    // thread che attende pacchetti da ev3
    private static class ReadEvents implements Runnable {
        private Activity a;
        private boolean run = true;

        public ReadEvents(Activity a) {
            this.a = a;
        }

        public void terminate() {
            this.run = true;
        }

        @Override
        public void run() {
            try {
                while(run) {
                    final Packet p;

                    // non server synchronized perchè è bloccante giÃ  di suo ed Ã¨ l'unica che riceve
                    p = (Packet)inputStream.readObject();

                    int key = p.getKey();
                    String message = p.getMessage();
                    // in base al pacchetto faccio qualcosa...
                    switch(key) {
                        case Packet.KEY_BATTERY:
                            double value = Double.valueOf(message);
                            changeBatteryIcon(a, value);
                            break;

                        case Packet.KEY_INFO_EXP:
                            String[] arr = message.split(";");

                            int currentPasso = Integer.valueOf(arr[0]);
                            int finPasso = Integer.valueOf(arr[1]);
                            String text = arr[2];

                            changeInfoBar(a, currentPasso, finPasso, text);

                            break;

                        case Packet.KEY_ERROR:
                            error(a, message);
                            break;

                        case Packet.KEY_DISCONNECT:
                            disconnect(false);
                            break;

                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // booleana che ritorna se è connesso o no
    public static boolean isConnected() {
        return connesso;
    }

    // controllo se bluetooth è supportato e se è attivo
    private static boolean checkBluetooth(final Activity a) {
        BluetoothHelper.BTAdapter = BluetoothAdapter.getDefaultAdapter();

        // Phone does not support Bluetooth so let the user know and exit.
        if (BluetoothHelper.BTAdapter == null) {
            new AlertDialog.Builder(a)
                    .setTitle(a.getText(R.string.key_bluetooth_supporto))
                    .setMessage(a.getText(R.string.key_telefono_supporto))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(R.drawable.warning)
                    .show();
            return false;
        } else if (!BluetoothHelper.BTAdapter.isEnabled()) {
            new AlertDialog.Builder(a)
                    .setTitle(a.getText(R.string.key_bluetooth_disabilitato))
                    .setMessage(a.getText(R.string.key_attiva_bluetooth))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(R.drawable.warning)
                    .show();

            return false;
        }
        return true;
    }

    // disconnessione
    public static void disconnect(boolean mandaPack) throws IOException {

        // gli dico di disconnettersi
        if(mandaPack)
            send(new Packet(Packet.KEY_DISCONNECT, "Close"));

        // chiudo thread e stream vari
        if(read != null) {
            read.interrupt();
        }

        synchronized (inputStream){
            if(inputStream != null) {
                try {
                    inputStream.close();
                }catch (Exception ex) {

                }
            }
        }

        if(readEvents != null) {
            readEvents.terminate();
        }

        if(outputStream != null) {
            try {
                outputStream.close();
            }catch (Exception ex) {

            }
        }

        if(mSocket != null) {
            try {
                mSocket.close();
            }catch (Exception ex) {

            }
        }

        mSocket = null;

        // non sono più connesso
        connesso = false;
    }

    // prendo i dispositivi associati
    public static ArrayList<BluetoothDevice> getBondedDevices() {
        ArrayList<BluetoothDevice> devices = new ArrayList<>();

        if(BTAdapter == null)
            return devices;

        devices.addAll(BTAdapter.getBondedDevices());

        return devices;
    }

    // carico i dispositivi su un dialog, quando si chiude se posso mi connetto
    private static void loadDisp(final Activity act) {
        final DialogListAdapter cdd = new DialogListAdapter(act);
        cdd.show();

        cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                final BluetoothDevice res = cdd.getSelected();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connect(res, act);
                        } catch (IOException e) {
                            error(act, act.getText(R.string.key_connection_error).toString());
                        }
                    }
                });

                t.start();
            }
        });
    }

    // se è tutto ok apro i dispositivi
    public static void scegliDispositivo(final Activity act) {
        boolean ok = checkBluetooth(act);

        if(ok)
            loadDisp(act);
    }

    // aggiorno testo batteria
    private static void changeBatteryIcon(final Activity act, final double value) {
        act.runOnUiThread(new Runnable() {
            @SuppressLint("RestrictedApi")
            @Override
            public void run() {
                try {
                    ActionMenuItemView m = act.findViewById(R.id.battery);
                    m.setTitle(value + " %");
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // aggiorno informazioni su exp inviata
    private static void changeInfoBar(final Activity act, final int currentPasso, final int finPasso, final String text) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // se posso, vibro
                if(Salvataggi.getVibrazione(act)) {
                    Vibrator v = (Vibrator) act.getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(400);
                }

                TextView info = act.findViewById(R.id.infoBar);
                TextView infoTesto = act.findViewById(R.id.infoTesto);
                ProgressBar pBar = act.findViewById(R.id.progressBar);

                pBar.setProgress(currentPasso);
                pBar.setMax(finPasso);

                info.setText(currentPasso + "/" + finPasso);
                infoTesto.setText(text);

                if(currentPasso == finPasso) {
                    Button btn = act.findViewById(R.id.invia);
                    btn.setEnabled(true);
                }
            }
        });
    }

    // messaggio di errore generico
    private static void error(final Activity act, final String text) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(act)
                        .setTitle(act.getText(R.string.key_error))
                        .setMessage(text)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(R.drawable.warning)
                        .show();
            }
        });
    }
}
