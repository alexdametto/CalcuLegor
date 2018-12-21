package Lego;

import java.io.Serializable;

public class Packet implements Serializable{

    public static final int KEY_BATTERY = 1;
    public static final int KEY_EXP = 50;

    private int key;
    private String message;
    private static final long serialVersionUID = 10;


    public Packet(int key, String message) {
        this.key = key;
        this.message = message;
    }

    public int getKey() {
        return this.key;
    }

    public String getMessage() {
        return this.message;
    }
}