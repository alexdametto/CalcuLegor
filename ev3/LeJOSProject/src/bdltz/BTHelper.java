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
	
	
	private static int DELAY_BATTERY = 1;
	
	BatteryInfo b;
	ReadEvents r;
	
	
	public BTHelper() {
		connector = new BTConnector();

		System.out.println("In attesa del dispositivo.");
		
		conn = connector.waitForConnection(timeoutCreationConnection, NXTConnection.RAW);
		
		System.out.println("Dispositivo connesso.");
		
		try {
			output = new ObjectOutputStream(conn.openOutputStream());
			output.flush();
			input = new ObjectInputStream(conn.openDataInputStream());	
		}catch(IOException ex) {
			ex.printStackTrace();
			return;
		}
		
		b = new BatteryInfo();
		r = new ReadEvents();
		
		Thread read = new Thread(r);
		read.start();
		
		Thread batt = new Thread(b);
		batt.start();
	}
	
	public void send(Packet pack) throws IOException {
		// INVIO CON SINCRONIZZAZIONE
		synchronized(output) {
			output.writeObject(pack);
			output.flush();
		}
	}
	
	
	private class BatteryInfo implements Runnable {
		private boolean run = true;
		
		public void terminate() {
			this.run = false;
		}
		
		@Override
		public void run() {
			
		    Power power = BrickFinder.getDefault().getPower();
			
			try {
				while(run) {
					// getVoltage : 2.5 = x : 100
					
					double perc = (power.getVoltage()-6.5) * 100 / 2.5;
					
					DecimalFormat df = new DecimalFormat("#.00");
					
					Packet pack = new Packet(Packet.KEY_BATTERY, df.format(perc));
					
					send(pack);
					
					TimeUnit.SECONDS.sleep(DELAY_BATTERY);
				}
			} catch (Exception e) {
				disconnect();
			}
			
		}
	}
	
	public void disconnect() {
		b.terminate();
		r.terminate();
	}
	
	
	private class ReadEvents implements Runnable {
		private boolean run = true;
		
		public void terminate() {
			this.run = false;
		}
		
		@Override
		public void run() {
			try {
				while(run) {
					Packet p;
					
					// non server synchronized perchè è bloccante giÃ  di suo ed Ã¨ l'unica che riceve
					p = (Packet)input.readObject();
					
					int key = p.getKey();
                    String message = p.getMessage();
										
					switch(key) {
						case Packet.KEY_EXP:
							
							SimplePrinter s = new SimplePrinter(message, BTHelper.this);
							s.startPrinting();
						
							break;
							
						case Packet.KEY_DISCONNECT:
							
							break;
					}
					
					// LANCIARE QUELLO CHE SERVE!!!!
					
				}
			} catch (Exception e) {
				disconnect();
			}
		}
	}
}
