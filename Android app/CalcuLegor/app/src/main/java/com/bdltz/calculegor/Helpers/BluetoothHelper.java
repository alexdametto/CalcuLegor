package com.bdltz.calculegor.Helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.ParcelUuid;
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

    private static BluetoothAdapter BTAdapter;
    private static BluetoothSocket mSocket;
    private static ObjectOutputStream outputStream;
    private static ObjectInputStream inputStream;

    private static ReadEvents readEvents;

    public static void connect(BluetoothDevice device, Activity a) throws IOException {
        if(device == null)
            return;

        ParcelUuid[] uuids = device.getUuids();
        mSocket =  device.createRfcommSocketToServiceRecord(uuids[0].getUuid());

        mSocket.connect();

        outputStream = new ObjectOutputStream(mSocket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(mSocket.getInputStream());

        // invia packet con le impostazioni!!!!!!

        readEvents = new ReadEvents(a);

        Thread t = new Thread(readEvents);
        t.start();
    }

    public static void send(Packet packet) throws IOException {
        outputStream.writeObject(packet);
        outputStream.flush();
    }

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

                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isConnected() {
        if(mSocket == null)
            return false;
        else return mSocket.isConnected();
    }

    private static AlertDialog checkBluetooth(final Activity a) {
        BluetoothHelper.BTAdapter = BluetoothAdapter.getDefaultAdapter();

        // Phone does not support Bluetooth so let the user know and exit.
        if (BluetoothHelper.BTAdapter == null) {
            return new AlertDialog.Builder(a)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else if (!BluetoothHelper.BTAdapter.isEnabled()) {
            return new AlertDialog.Builder(a)
                    .setTitle("Bluetooth non attivo")
                    .setMessage("Il bluetooth non è attivo. Attivalo e riprova.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return null;
    }

    // invocarlo quando si chiude app
    public static void disconnect() throws IOException {
        // send messaggio chiusura!!!

        send(new Packet(Packet.KEY_DISCONNECT, "Close"));

        synchronized (inputStream){
            if(inputStream != null)
                inputStream.close();
        }

        if(readEvents != null) {
            readEvents.terminate();
        }

        synchronized (outputStream){
            if(inputStream != null)
                outputStream.close();
        }

        if(mSocket != null)
            mSocket.close();

        mSocket = null;

        // reset grafico!!!!!!
    }

    public static ArrayList<BluetoothDevice> getBondedDevices() {
        ArrayList<BluetoothDevice> devices = new ArrayList<>();

        if(BTAdapter == null)
            return devices;

        devices.addAll(BTAdapter.getBondedDevices());

        return devices;
    }

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
                            error(act, "Errore in connessione");
                        }
                    }
                });

                t.start();
            }
        });
    }

    public static void scegliDispositivo(final Activity act) {
        // controllare se bluetooth è attivo oppure no !!!!
        AlertDialog a = checkBluetooth(act);

        if(a == null)
            loadDisp(act);
    }

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


    private static void changeInfoBar(final Activity act, final int currentPasso, final int finPasso, final String text) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView info = act.findViewById(R.id.infoBar);
                ProgressBar pBar = act.findViewById(R.id.progressBar);

                pBar.setProgress(currentPasso);
                pBar.setMax(finPasso);

                info.setText(currentPasso + "/" + finPasso);

                if(currentPasso == finPasso) {
                    Button btn = act.findViewById(R.id.invia);
                    btn.setEnabled(true);
                }
            }
        });
    }

    private static void error(final Activity act, final String text) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(act)
                        .setTitle("Errore")
                        .setMessage(text)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }
}
