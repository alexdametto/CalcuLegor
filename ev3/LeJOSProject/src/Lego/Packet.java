package Lego;

import java.io.Serializable;

public class Packet implements Serializable{
	
	// vari tipi di pacchetti
	public static final int KEY_BATTERY = 1;
    public static final int KEY_IMPOSTAZIONI = 2;
	public static final int KEY_EXP = 50;
	public static final int KEY_INFO_EXP = 51;
	public static final int KEY_ERROR = 100;
    public static final int KEY_DISCONNECT = 200;
	
    // contenuto del pacchetto
	private int key;
	private String message;
	
	// per il Serializable...
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
