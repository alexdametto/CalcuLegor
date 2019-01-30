package bdltz;

import Lego.Packet;
import dametto.alex.Exp;
import dametto.alex.Step;
import dametto.alex.Steps;
import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

public class SimplePrinter {	
	private String toPrint;
	
	// da rivedere
	private static final double PAPER_MAX_X = 6.25;
	private static final double PAPER_MAX_Y = 20; 
	
	private static final double LETTER_MAX_X = 0.5; 
	private static final double LETTER_MAX_Y = 1;
	private static final double DELAY_X = 0.25;
	private static final double DELAY_Y = 0.25;
	
	private static final double PAPER_SENS = 0.04; 
		
	final private int charForRow = (int) Math.floor(( PAPER_MAX_X - 2 * DELAY_X ) / (LETTER_MAX_X + DELAY_X));
	private int indexInRow = 0;
	
	final private int numberRow = (int) Math.floor(( PAPER_MAX_Y - 2 * DELAY_Y ) / (LETTER_MAX_Y + DELAY_Y)) ;
	private int indexRow = 0;
	
	// necessitano di una rotazione di degreePerN per fare 1 cm di movimento nell'asse N
	final private double degreePerX = 111.111111; 
	final private double degreePerY = 90.909090;
	
	// angolo per alzare la penna
	final private int degreePerZ = 280;
	
	// posizione dentro la lettera
	private double currentX = 0;
	private double currentY = 0;
	
	// current angolo Z
	private int currentZ = degreePerZ;

	// sensori
	private static final EV3UltrasonicSensor us = new EV3UltrasonicSensor(SensorPort.S1);
	private static final EV3TouchSensor touch = new EV3TouchSensor(SensorPort.S2);
	private final SampleProvider sp = us.getDistanceMode();
	
	private int defaultSpeed = 360; // 360 degress per seconds
	
	// booleana che indica se sto stampando o meno
	private boolean printing = false;
	
	// connessione bluetooth
	BTHelper bt;
	
	SimplePrinter(String toPrint, BTHelper bt) {
		this.bt = bt;
		this.toPrint = toPrint;
	}
	
	// metodo di iniziare a stampare
	public void startPrinting() {
		// aspetto il foglio
		attendiFoglio();
				
		// valuto l'espressione e se ci sono errori lo ritorno
		Exp e = new Exp(toPrint);
		Steps passi = null;
		
		try {
			e.parse();
			passi = e.valutaPassoAPasso();
		} catch (Exception e1) {
			Packet p = new Packet(Packet.KEY_ERROR, e1.getMessage());
			try {
				bt.send(p);
			} catch (Exception e2) {
				
			}
		}
		
		int index = 0;
		
		int totalPassi = 0;
		
		// conto i passi di calcoli
		for(Step a : passi.getSteps()) {
			if(!a.getDescription().equals(""))
				totalPassi++;
		}
		
		printing = true;
		
		int nPasso = 0;
		// scorro tutti i passi
		for(Step a : passi.getSteps()) {
			// se è un passo di calcolo
			if(!a.getDescription().equals("")) {
				String passo = a.getExp().toLowerCase();
				String description = a.getDescription().toLowerCase();
				
				// se non sono all'ultimo passo aggiungi il simbolo "="
				if(nPasso != totalPassi)
					passo += "=";
							
				// inzia informazione su cosa sta facendo
				Packet pack = new Packet(Packet.KEY_INFO_EXP, (nPasso+1) + ";" + totalPassi +  ";" + description);
				try {
					bt.send(pack);
				} catch (Exception e1) {}

				// per ogni carattere, stampalo
				for(int i = 0; i < passo.length(); ++i) {
					moveInsideLetter(0, 0);
					char c = passo.charAt(i);
					
					printChar(c);
							
					// aggiorna valori e controlla se ho spazio nel foglio
					indexInRow++;
					if(indexInRow > charForRow) {
						// finita la riga
						// andare a capo
						
						indexRow++;
						indexInRow = 0;
					}
					if(indexRow > numberRow) {
						// finito il foglio
						cambiaFoglio();
						indexRow = 0;
						indexInRow = 0;
					}
				}
				
				// ho finito di stampare il passo, vado a capo e faccio controlli
				indexInRow = 0;
				indexRow++;
				
				if(indexRow > numberRow) {
					cambiaFoglio();
					indexRow = 0;
					indexInRow = 0;
				}
				
				index++;
				
				moveInsideLetter(0, 0);
				
				// suona se hai il permesso di suonare
				if(Salvataggi.getAudio()) {
					Sound.twoBeeps();
				}
				
				// aspetta il click se l'utente lo desidera, tranne per l'ultimo passo perchè hai finito
				if(Salvataggi.getClickProcedere() && nPasso != totalPassi) {
					float value;
					do {
						int sampleSize = touch.sampleSize();
						float[] sample = new float[sampleSize];
						touch.fetchSample(sample, 0);
						value = sample[0];
					} while(value != 1);
				}
				nPasso++;
			}
		}
			
		// alza la penna e cambia foglio che hai finito
		indexInRow = 0;
		moveInsideLetter(0, 0);
				
		if(currentZ != degreePerZ) {
			Motor.C.rotate((int)(degreePerZ-currentZ)); // prima bisogna spostare l'asse X
			currentZ = degreePerZ;
		}
		
		printing = false;
		
		espelliFoglio();
	}
	
	// ritorna se sta stampando o no
	public boolean isPrinting() {
		return this.printing;
	}
	
	// espelle il foglio
	private void espelliFoglio() {		
		float distanceValue;
		Motor.B.setSpeed(defaultSpeed);
		Motor.B.forward();
        do {
    		float [] sample = new float[sp.sampleSize()];
            sp.fetchSample(sample, 0);
            distanceValue = sample[0];
        } while(distanceValue < PAPER_SENS || Float.isInfinite(distanceValue));
        
        Motor.B.stop();
        
        Motor.B.rotate((int)(degreePerY * 15));
	}
	
	// attendi un foglio
	private void attendiFoglio() {		
		float distanceValue;
		Motor.B.setSpeed(defaultSpeed);
		Motor.B.backward();
        do {
    		float [] sample = new float[sp.sampleSize()];
            sp.fetchSample(sample, 0);
            distanceValue = sample[0];
            
        } while(distanceValue > PAPER_SENS && !Float.isInfinite(distanceValue));
        
        Motor.B.stop();
	}

	// espelli e attenti un foglio
	private void cambiaFoglio() {
		if(currentZ != degreePerZ) {
			Motor.C.rotate((int)(degreePerZ-currentZ)); // prima bisogna spostare l'asse X
			currentZ = degreePerZ;
		}
		
		espelliFoglio();
		attendiFoglio();
	}
	
	// stampa carattere
	private void printChar(char c) {
		switch(c) {
			case 'a' :
				printA();
				break;
			case 'b' :
				printB();
				break;
			case 'c' :
				printC();
				break;
			case 'd' :
				printD();
				break;
			case 'e' :
				printE();
				break;
			case 'f' :
				printF();
				break;
			case 'g' :
				printG();
				break;
			case 'h' :
				printH();
				break;
			case 'i' :
				printI();
				break;
			case 'j' :
				printJ();
				break;
			case 'k' :
				printK();
				break;
			case 'l' :
				printL();
				break;
			case 'm' :
				printM();
				break;
			case 'n' :
				printN();
				break;
			case 'o' :
				printO();
				break;
			case 'p' :
				printP();
				break;
			case 'q' :
				printQ();
				break;
			case 'r' :
				printR();
				break;
			case 's' :
				printS();
				break;
			case 't' :
				printT();
				break;
			case 'u' :
				printU();
				break;
			case 'v' :
				printV();
				break;
			case 'x' :
				printX();
				break;
			case 'y' :
				printY();
				break;
			case 'w' :
				printW();
				break;
			case 'z' :
				printZ();
				break;
				
			case '0' :
				print0();
				break;
			case '1' :
				print1();
				break;
			case '2' :
				print2();
				break;
			case '3' :
				print3();
				break;
			case '4' :
				print4();
				break;
			case '5' :
				print5();
				break;
			case '6' :
				print6();
				break;
			case '7' :
				print7();
				break;
			case '8' :
				print8();
				break;
			case '9' :
				print9();
			break;
			
			case '+' :
				printSomma();
				break;
			case '-' :
				printSottrazione();
				break;
			case '*' :
				printMoltiplicazione();
				break;
			case '/' :
				printDivisione();
				break;
			case '^' :
				printPotenza();
				break;
			case '√' :
				printRadice();
				break;
			case '=' :
				printUguale();
				break;
			
			case '(' :
				printATonda();
				break;
			case ')' :
				printCTonda();
				break;
			case '[' :
				printAQuadra();
				break;
			case ']' :
				printCQuadra();
				break;
			case '{' :
				printAGraffa();
				break;
			case '}' :
				printCGraffa();
				break;
			case '.' :
				printDot();
				break;
				
		}
	}
	
	// ritorna la posizione globale della X nel foglio, avendo la posizione nella lettera
	private double getGlobalX(double x) {		
		return x + (double)(indexInRow) * (LETTER_MAX_X + DELAY_X);
	}
	
	// ritorna la posizione globale della Y nel foglio, avendo la posizione nella lettera
	private double getGlobalY(double y) {
		return y + (double)(indexRow) * (LETTER_MAX_Y + DELAY_Y);
	}

	// effettua un movimento da dove sta ora fino a dest
	private void simpleMove(final double destX, final double destY) {		
		final double dx = Math.abs(currentX - getGlobalX(destX));
		final double dy = Math.abs(currentY - getGlobalY(destY));
				
		// Non abbiamo bisogno di movimento
		if(dx == 0 && dy == 0)
			return;
		
		int speedDx = defaultSpeed;
		int speedDy = defaultSpeed;
		
		if(dx > dy && dy != 0) {			
			// dy ha meno spazio da fare, dx deve velocizzarsi per fare più spazio in meno tempo.
			// vel = spazio / tempo 	=> tempo = spazio / vel
			
			// tempoDx = tempoDy 		=> spazioDx / velDx = spazioDy / velDy
			speedDx = (int) Math.round(speedDy / dy * dx);
		}
		else if(dx < dy && dx != 0) {
			// dx ha meno spazio da fare, dy deve velocizzarsi per fare più spazio in meno tempo.
			// vel = spazio / tempo 	=> tempo = spazio / vel
			
			// tempoDx = tempoDy 		=> spazioDx / velDx = spazioDy / velDy
			speedDy = (int) Math.round(speedDx / dx * dy);
		}
		
		// Setto le velocità
		Motor.A.setSpeed(speedDx);
		Motor.B.setSpeed(speedDy);

		// Muovo entrambi i motori
		Thread t1 = new Thread(new Runnable() {
		     @Override
		     public void run() {
		    	 if(dx != 0)
		    		 Motor.A.rotate((int)Math.round(degreePerX * (getGlobalX(destX) - currentX))); // il contrario forse?
		     }
		});
		
		Thread t2 = new Thread(new Runnable() {
		     @Override
		     public void run() {  
		    	 if(dy != 0)
		    		 Motor.B.rotate(-(int)Math.round(degreePerY * (getGlobalY(destY) - currentY))); // il contrario forse?
		     }
		});
		
		t1.start();
		t2.start();
		
		try {
			// Attendo....

			t1.join();
			t2.join();	
			
			// aggiorno i valori
			currentX = getGlobalX(destX);
			currentY = getGlobalY(destY);
		}catch(Exception e) {
			System.out.println("Error executing parallel motors move. Contact the productor.");
		}
	}
	
	
	// Mi muovo dentro la lettera
	private void moveInsideLetter(double destX, double destY) {
		// alzo la penna
		if(currentZ == 0) {
			Motor.C.rotate(degreePerZ);
			currentZ = degreePerZ;
		}
					
		simpleMove(destX, destY);
	}
	
	// Effettuo una linea
	private void lineInsideLetter(double destX, double destY) {
		// abbasso la penna
		if(currentZ != 0) {
			Motor.C.rotate((int)-currentZ);
			currentZ = 0;
		}
				
		simpleMove(destX, destY);
	}
	
	
	
	
	private void printA() {
		moveInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(0, 0);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		moveInsideLetter(0, LETTER_MAX_Y / 2);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y / 2);
	}
	
	private void printB() {
		lineInsideLetter(0,LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.75);
		lineInsideLetter(0,LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.25);
		lineInsideLetter(0,0);
	}
	
	private void printC() {
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		moveInsideLetter(0, 0);
		lineInsideLetter(LETTER_MAX_X, 0);
	}
	
	private void printD() {
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		lineInsideLetter(0, 0);
	}
	
	private void printE() {
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		moveInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		moveInsideLetter(0, 0);
		lineInsideLetter(LETTER_MAX_X, 0);
	}
	
	private void printF() {
		lineInsideLetter(0, LETTER_MAX_Y);
		moveInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		moveInsideLetter(0, 0);
		lineInsideLetter(LETTER_MAX_X, 0);
	}
	
	private void printG() {
		moveInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(0, 0);
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y * 0.5);
	}
	
	private void printH() {
		lineInsideLetter(0, LETTER_MAX_Y);
		moveInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		moveInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
	}
	
	private void printI() {
		lineInsideLetter(LETTER_MAX_X, 0);
		moveInsideLetter(LETTER_MAX_X * 0.5, 0);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
		moveInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);		
	}
	
	private void printJ() {
		lineInsideLetter(LETTER_MAX_X, 0);
		moveInsideLetter(LETTER_MAX_X * 0.5, 0);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
		lineInsideLetter(0,0);
	}
	
	private void printK() {
		lineInsideLetter(0, LETTER_MAX_Y);
		moveInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, 0);
		moveInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(0, LETTER_MAX_Y);
	}
	
	private void printL() {
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);		
	}
	
	private void printM() {
		moveInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(0, 0);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y * 0.25);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
	}
	
	private void printN() {
		moveInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(0, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, 0);
	}
	
	private void printO() {
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(0, 0);
	}
	
	private void printP() {
		moveInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(0, 0);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
	}
	
	private void printQ() {
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(0, 0);
		moveInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y * 0.75);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
	}
	
	private void printR() {
		moveInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(0, 0);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
	}
	
	private void printS() {
		moveInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.25);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(0, 0);
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(0, LETTER_MAX_Y * 0.75);
	}
	
	private void printT() {
		lineInsideLetter(LETTER_MAX_X, 0);
		moveInsideLetter(LETTER_MAX_X * 0.5, 0);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
	}
	
	private void printU() {
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, 0);		
	}
	
	private void printV() {
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, 0);		
	}
	
	private void printX() {
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		moveInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, 0);
	}
	
	private void printW() {
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X * 0.25, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X * 0.50, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X * 0.75, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, 0);
	}
	
	private void printY() {
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, 0);
		moveInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
	}
	
	private void printZ() {
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		moveInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
	}
	
	private void print0() {
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(0, 0);
	}
	
	private void print1() {
		moveInsideLetter(0, LETTER_MAX_Y * 0.25);
		lineInsideLetter(LETTER_MAX_X * 0.5, 0);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
		moveInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
	}
	
	private void print2() {
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
	}
	
	private void print3() {
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(0, LETTER_MAX_Y);
		moveInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
	}
	
	private void print4() {
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		moveInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
	}
	
	private void print5() {
		moveInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(0, 0);
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(0, LETTER_MAX_Y);
	}
	
	private void print6() {
		moveInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(0, 0);
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
	}
	
	private void print7() {
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
	}
	
	private void print8() {
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(0, 0);
		moveInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
	}
	
	private void print9() {
		moveInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(0, 0);
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
	}
	
	private void printSomma() {
		moveInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		moveInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y * 0.25);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y * 0.75);
	}
	
	private void printSottrazione() {
		moveInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
	}
	
	private void printMoltiplicazione() {
		moveInsideLetter(LETTER_MAX_X * 0.25, LETTER_MAX_Y * 0.375);
		lineInsideLetter(LETTER_MAX_X * 0.25, LETTER_MAX_Y * 0.625);
		lineInsideLetter(LETTER_MAX_X * 0.75, LETTER_MAX_Y * 0.625);
		lineInsideLetter(LETTER_MAX_X * 0.75, LETTER_MAX_Y * 0.375);
		lineInsideLetter(LETTER_MAX_X * 0.25, LETTER_MAX_Y * 0.375);	
	}
	
	private void printDivisione() {
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
	}
	
	private void printPotenza() {
		moveInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X * 0.5, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
	}
	
	private void printUguale() {
		moveInsideLetter(0, LETTER_MAX_Y * 0.25);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.25);
		moveInsideLetter(0, LETTER_MAX_Y * 0.75);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.75);
	}
	
	private void printRadice() {
		moveInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, 0);
	}
	
	private void printATonda() {
		moveInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
	}
	
	private void printCTonda() {
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		lineInsideLetter(0, LETTER_MAX_Y);
	}
	
	private void printAQuadra() {
		moveInsideLetter(LETTER_MAX_X * 0.5, 0);
		lineInsideLetter(0, 0);
		lineInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
	}
	
	private void printCQuadra() {
		moveInsideLetter(LETTER_MAX_X * 0.5, 0);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
	}
	
	private void printAGraffa() {
		moveInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X * 0.5, 0);
		lineInsideLetter(0, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
	}
	
	private void printCGraffa() {
		lineInsideLetter(LETTER_MAX_X * 0.5, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
		lineInsideLetter(0, LETTER_MAX_Y);
	}
	
	private void printDot(){
		moveInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.75);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y * 0.75);
		lineInsideLetter(LETTER_MAX_X * 0.5, LETTER_MAX_Y);
	}
}
