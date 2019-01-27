package com.bdltz.calculegor.Helpers;

import android.app.Activity;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class Salvataggi {
    public static boolean getAudio(Activity a) {
        SharedPreferences prefs = a.getSharedPreferences("SALVATAGGI", MODE_PRIVATE);
        boolean value = prefs.getBoolean("audio", false);
        return value;
    }

    public static boolean getVibrazione(Activity a) {
        SharedPreferences prefs = a.getSharedPreferences("SALVATAGGI", MODE_PRIVATE);
        boolean value = prefs.getBoolean("vibrazione", false);
        return value;
    }

    public static boolean getClickProcedere(Activity a) {
        SharedPreferences prefs = a.getSharedPreferences("SALVATAGGI", MODE_PRIVATE);
        boolean value = prefs.getBoolean("pulsante", false);
        return value;
    }

    public static void setAudio(Activity a, boolean value) {
        SharedPreferences.Editor editor = a.getSharedPreferences("SALVATAGGI", MODE_PRIVATE).edit();
        editor.putBoolean("audio", value);
        editor.apply();
    }

    public static void setVibrazione(Activity a, boolean value) {
        SharedPreferences.Editor editor = a.getSharedPreferences("SALVATAGGI", MODE_PRIVATE).edit();
        editor.putBoolean("vibrazione", value);
        editor.apply();
    }

    public static void setClickProcedere(Activity a, boolean value) {
        SharedPreferences.Editor editor = a.getSharedPreferences("SALVATAGGI", MODE_PRIVATE).edit();
        editor.putBoolean("pulsante", value);
        editor.apply();
    }
}
