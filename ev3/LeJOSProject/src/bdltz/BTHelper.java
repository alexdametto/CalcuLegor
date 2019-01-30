package bdltz;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import Lego.Packet;
import lejos.hardware.BrickFinder;
import lejos.hardware.Power;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.remote.nxt.BTConnector;
import lejos.remote.nxt.NXTConnection;

public class BTHelper {
	
	// variabili locali per gestire la connessione
	private BTConnector connector;
	private NXTConnection conn;
	private int timeoutCreationConnection = 0;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	// vari thread
	private ReadEvents readEvents;
	private Thread read;
	private BatteryInfo battery;
	
	// il printer
	private SimplePrinter s = null;
	
	// ogni DELAY_BATTERY secondi invia informazione batteria
	private static int DELAY_BATTERY = 1;
	
	// sensore touch
	private static final EV3TouchSensor touch = new EV3TouchSensor(SensorPort.S4);
	
	
	public BTHelper() {
		// crea e aspetta una connessione
		connector = new BTConnector();

		System.out.println("In attesa del dispositivo.");
		
		conn = connector.waitForConnection(timeoutCreationConnection, NXTConnection.RAW);
		
		System.out.println("Dispositivo connesso.");
		
		// apri gli stream
		try {
			output = new ObjectOutputStream(conn.openOutputStream());
			output.flush();
			input = new ObjectInputStream(conn.openDataInputStream());	
		}catch(IOException ex) {
			ex.printStackTrace();
			return;
		}
		
		// avvia i vari thread
		battery = new BatteryInfo();
		readEvents = new ReadEvents();
		
		read = new Thread(readEvents);
		read.start();
		
		Thread batt = new Thread(battery);
		batt.start();
		
		Thread disc = new Thread(new Disconnect());
		disc.start();
	}
	
	// invia un pacchetto
	public void send(Packet pack) throws IOException {
		// INVIO CON SINCRONIZZAZIONE
		synchronized(output) {
			output.writeObject(pack);
			output.flush();
		}
	}
	
	// thread che monitora la batteria e invia al device
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
	
	// disconnette
	public void disconnect(boolean mandaPack) throws IOException {
        // send messaggio chiusura!!!
        if(mandaPack)
            send(new Packet(Packet.KEY_DISCONNECT, "Close"));
        
        // interrompi tutti i thread e chiudi stream
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
        
        // chiudi il programma
        System.exit(0);
    }
	
	// thread che attende dei pacchetti dal device
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
					
					p = (Packet)input.readObject();
					
					int key = p.getKey();
                    String message = p.getMessage();
					// in base al pacchetto fai qualcosa		
					switch(key) {
						case Packet.KEY_EXP:
							
							s = new SimplePrinter(message, BTHelper.this);
							s.startPrinting();
						
							break;
							
						case Packet.KEY_DISCONNECT:
							
							disconnect(false);
							
							break;
							
						case Packet.KEY_IMPOSTAZIONI:
							
							String[] valori = message.split(";");
							Salvataggi.setAudio(Boolean.parseBoolean(valori[0]));
							Salvataggi.setClickProcedere(Boolean.parseBoolean(valori[1]));
							
							break;
					}					
				}
			} catch (Exception e) {
				try {
					disconnect(false);
				} catch (IOException e1) {
				}
			}
		}
	}
	
	
	// thread che attende se ha premuto touch del disconnetti
	private class Disconnect implements Runnable {
		
		@Override
		public void run() {
			float value;
			do {
				int sampleSize = touch.sampleSize();
				float[] sample = new float[sampleSize];
				touch.fetchSample(sample, 0);
				value = sample[0];
			} while(value != 1);
			
			// se sta stampando aspetta....
			if(s != null) {
				while(s.isPrinting()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						
					}
				}
			}
			
			// disconnetti
			try {
				disconnect(true);
			} catch (IOException e) {
				
			}
		}
	}
}
