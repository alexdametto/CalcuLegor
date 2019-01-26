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
	
	private ReadEvents readEvents;
	private Thread read;
	private BatteryInfo battery;
	
	private static int DELAY_BATTERY = 1;
	
	
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
		
		battery = new BatteryInfo();
		readEvents = new ReadEvents();
		
		read = new Thread(readEvents);
		read.start();
		
		Thread batt = new Thread(battery);
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
					
					DecimalFormat df = new DecimalFormat("#");
					
					Packet pack = new Packet(Packet.KEY_BATTERY, df.format(perc));
					
					send(pack);
					
					TimeUnit.SECONDS.sleep(DELAY_BATTERY);
				}
			} catch (Exception e) {
				try {
					disconnect(false);
				} catch (IOException e1) {
				}
			}
			
		}
	}
	
	public void disconnect(boolean mandaPack) throws IOException {
        // send messaggio chiusura!!!

        if(mandaPack)
            send(new Packet(Packet.KEY_DISCONNECT, "Close"));
        
        if(read != null)
        	read.interrupt();

        synchronized (input){
            if(input != null)
            	input.close();
        }

        if(readEvents != null) {
            readEvents.terminate();
        }
        
        if(battery != null) {
        	battery.terminate();
        }

        synchronized (output){
            if(output != null)
            	output.close();
        }

        if(conn != null)
            conn.close();

        conn = null;
        
        System.exit(0);

        // reset grafico!!!!!!
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
					
					
					// non server synchronized perch� � bloccante già di suo ed è l'unica che riceve
					p = (Packet)input.readObject();
					
					int key = p.getKey();
                    String message = p.getMessage();
										
					switch(key) {
						case Packet.KEY_EXP:
							
							SimplePrinter s = new SimplePrinter(message, BTHelper.this);
							s.startPrinting();
						
							break;
							
						case Packet.KEY_DISCONNECT:
							
							disconnect(false);
							
							break;
					}
					
					// LANCIARE QUELLO CHE SERVE!!!!
					
				}
			} catch (Exception e) {
				try {
					disconnect(false);
				} catch (IOException e1) {
				}
			}
		}
	}
}
