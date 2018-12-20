package bdltz;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import Lego.Packet;
import lejos.hardware.BrickFinder;
import lejos.hardware.Power;
import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;

public class BTHelper {
	
	private BTConnector connector;
	private NXTConnection conn;
	private int timeoutCreationConnection = 0;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	
	private static int DELAY_BATTERY = 10;
	
	
	public BTHelper() {
		connector = new BTConnector();

		System.out.println("In attesa del dispositivo.");
		
		conn = connector.waitForConnection(timeoutCreationConnection, NXTConnection.RAW);
		
		System.out.println("Dispositivo connesso.");
		
		try {
			output = new ObjectOutputStream(conn.openOutputStream());
			input = new ObjectInputStream(conn.openDataInputStream());	
		}catch(IOException ex) {
			ex.printStackTrace();
			return;
		}
		
		Thread read = new Thread(new ReadEvents());
		read.start();
		
		Thread batt = new Thread(new BatteryInfo());
		batt.start();
	}
	
	
	private class BatteryInfo implements Runnable {

		@Override
		public void run() {
			
		    Power power = BrickFinder.getDefault().getPower();
			
			try {
				while(true) {
					
					// getVoltage : perc = 9 : 100
					
					double perc = power.getVoltage() * 9 / 100;
					
					DecimalFormat df = new DecimalFormat("#.00");
					
					Packet pack = new Packet(Packet.KEY_BATTERY, df.format(perc));
					
					// INVIO CON SINCRONIZZAZIONE
					synchronized(output) {
						output.writeObject(pack);
					}
					
					
					TimeUnit.SECONDS.sleep(DELAY_BATTERY);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	
	private class ReadEvents implements Runnable {
		@Override
		public void run() {
			
			try {
				while(true) {
					Packet p;
					
					// non server synchronized perchè è bloccante già di suo ed è l'unica che riceve
					p = (Packet)input.readObject();
					
					int key = p.getKey();
										
					switch(key) {
						case Packet.KEY_EXP:
							break;
					}
					
					// LANCIARE QUELLO CHE SERVE!!!!
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
