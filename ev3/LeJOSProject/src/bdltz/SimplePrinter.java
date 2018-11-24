package bdltz;

import java.util.ArrayList;

import bdltz.Printer.Point;
import lejos.hardware.motor.Motor;

public class SimplePrinter {
	private ArrayList<String> container;
	
	// FOGLI DA 14 cm di larghezza, lunghi come un A4 (quindi 14 cm x 29.7 cm)

	private static final double PAPER_CM_X = 14;
	private static final double PAPER_CM_Y = 29.7;
	
	// da rivedere
	private static final double PAPER_MAX_X = 1 * PAPER_CM_X;
	private static final double PAPER_MAX_Y = 1 * PAPER_CM_Y; 
	
	// a letter is LETTER_MAX_X * LETTER_MAX_Y rectangle.
	private static final double LETTER_MAX_X = 1; 
	private static final double LETTER_MAX_Y = 2;
	private static final double DELAY_X = 0.5;
	private static final double DELAY_Y = 0.5;
		
	// inside the letter....
	private double currentX = 0 / 2;
	private double currentY = 0;
	private double currentZ = 0; // da cambiare con il grado

	private int charForRow = (int) Math.floor(( PAPER_MAX_X - 2 * DELAY_X ) / (LETTER_MAX_X + DELAY_X));
	private int indexInRow = 0;
	
	private int numberRow = (int) Math.floor(( PAPER_MAX_Y - 2 * DELAY_Y ) / (LETTER_MAX_Y + DELAY_Y)) ;
	private int indexRow = 0;
	
	private double degreePerX = 111.1111;
	private double degreePerY = 111.1111;
	
	private int defaultSpeed = 720; // 720 degress per seconds
	
	SimplePrinter(ArrayList<String> toPrint) {
		this.container = toPrint;
	}
	
	SimplePrinter(String toPrint) {
		this.container = new ArrayList<String>();
		container.add(toPrint);
		
		System.out.println(charForRow + ", " + numberRow);
	}
	
	public void startPrinting() {
		for(String passo : container) {
			passo = passo.toLowerCase();
			
			for(int i = 0; i < passo.length(); ++i) {
				char c = passo.charAt(i);
				
				printChar(c);
				
				indexInRow++;
				// change to next with checkes...
				if(indexInRow > charForRow) {
					// finita la riga
					
					// andare a capo
					indexRow++;
				}
				if(indexRow > numberRow) {
					// finito il foglio
					
					// mando fuori questo foglio
					
					// aspetto il prossimo foglio
				}
			}
		}
	}
	
	private void printChar(char c) {
		switch(c) {
			case 'a' :
				printA();
				break;
			case 'b' :
				printB();
				break;
		}
	}
	
	private void simpleMove(final double destX, final double destY) {
		final double dx = Math.abs(currentX - destX);
		final double dy = Math.abs(currentY - destY);
		
		// We don't need a movement.
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
		
		Motor.A.setSpeed(speedDx);
		Motor.B.setSpeed(speedDy);

		// parallel move both motors
		
		Thread t1 = new Thread(new Runnable() {
		     @Override
		     public void run() {
		    	 if(dx != 0)
		    		 Motor.A.rotate((int)Math.round(degreePerX * (destX - currentX))); // il contrario forse?
		     }
		});
		
		Thread t2 = new Thread(new Runnable() {
		     @Override
		     public void run() {  
		    	 if(dy != 0)
		    		 Motor.B.rotate((int)Math.round(degreePerY * (destY - currentY))); // il contrario forse?
		     }
		});
		
		t1.start();
		t2.start();
		
		try {
			t1.join();
			t2.join();	
			
			currentX = destX;
			currentY = destY;
			
			// wait both thread before going out.
		}catch(Exception e) {
			System.out.println("Error executing parallel motors move.");
		}
	}
	
	
	// move from a point to a point at the same time and with the same duration
	// DOESN'T WORK, SOMETHING WRONG WITH SIGNS!!!!!!!!!!!
	private void moveInsideLetter(double destX, double destY) {
		// alza il motore se serve
		
		Motor.C.rotate(800);
				
		simpleMove(destX, destY);
		
		Motor.C.rotate(-800);
		// abbassa il motore
	}
	
	
	
	
	private void lineInsideLetter(double destX, double destY) {
		// same as above without taking up the pen 
		
		simpleMove(destX, destY);
		
	}
	
	
	private void printA() {
		/*	__
		 * |__|
		 * |  |
		 * 
		 */
		
		//moveInsideLetter(LETTER_MAX_X, 0);
		moveInsideLetter(0, LETTER_MAX_Y);
		lineInsideLetter(0, 0);
		lineInsideLetter(LETTER_MAX_X, 0);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
		moveInsideLetter(0, LETTER_MAX_Y / 2);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y / 2);
	}
	
	private void printB() {
		
	}
}
