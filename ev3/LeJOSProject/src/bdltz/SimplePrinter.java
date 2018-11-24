package bdltz;

import java.util.ArrayList;

import bdltz.Printer.Point;
import lejos.hardware.motor.Motor;

public class SimplePrinter {
	private ArrayList<String> container;
	
	// da rivedere
	private static final int PAPER_MAX_X = 100;
	private static final int PAPER_MAX_Y = 100; 
	
	// a letter is LETTER_MAX_X * LETTER_MAX_Y rectangle.
	private static final int LETTER_MAX_X = 5; 
	private static final int LETTER_MAX_Y = 10;
	private static final int DELAY_X = LETTER_MAX_X;
	private static final int DELAY_Y = LETTER_MAX_Y;
	
	private static final int DELAY_BETWEEN_LETTERS = 2;
	
	// inside the letter....
	private double currentX = LETTER_MAX_X / 2;
	private double currentY = 0;
	private double currentZ = 0; // da cambiare con il grado

	private int charForRow = ( PAPER_MAX_X - 2 * DELAY_X ) / (LETTER_MAX_X + DELAY_X) ;
	private int indexInRow = 0;
	
	private int numberRow = ( PAPER_MAX_Y - 2 * DELAY_Y ) / (LETTER_MAX_Y + DELAY_Y) ;
	private int indexRow = 0;
	
	private int degreePerX = 10;
	private int degreePerY = 10;
	
	private int defaultSpeed = 10; // 10 degress per seconds
	
	SimplePrinter(ArrayList<String> toPrint) {
		this.container = toPrint;
	}
	
	SimplePrinter(String toPrint) {
		this.container = new ArrayList<String>();
		container.add(toPrint);
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
	
	
	// move from a point to a point at the same time and with the same duration
	// DOESN'T WORK, SOMETHING WRONG WITH SIGNS!!!!!!!!!!!
	private void moveInsideLetter(final int destX, final int destY) {
		
		// alza il motore se serve
		
		final double dx = Math.abs(currentX - destX);
		final double dy = Math.abs(currentY - destY);
		
		// We don't need a movement.
		if(dx == 0 && dy == 0)
			return;
		
		int speedDx = defaultSpeed;
		int speedDy = defaultSpeed;
		
		if(dx > dy) {			
			// dy ha meno spazio da fare, dx deve velocizzarsi per fare più spazio in meno tempo.
			// vel = spazio / tempo 	=> tempo = spazio / vel
			
			// tempoDx = tempoDy 		=> spazioDx / velDx = spazioDy / velDy
			speedDx = (int) Math.round(speedDy / dy * dx);
		}
		else if(dx < dy) {
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
		    	 Motor.A.rotate((int)Math.round(degreePerX * (destX - currentX))); // il contrario forse?
		     }
		});
		
		Thread t2 = new Thread(new Runnable() {
		     @Override
		     public void run() {   	 
		    	 Motor.B.rotate((int)Math.round(degreePerY * (destY - currentY))); // il contrario forse?
		     }
		});
		
		t1.start();
		t2.start();
		
		try {
			t1.join();
			t2.join();
			
			// wait both thread before going out.
		}catch(Exception e) {
			System.out.println("Error executing parallel motors move.");
		}
		
		
		// abbassa il motore
	}
	
	
	
	
	private void lineInsideLetter(int x, int y) {
		// same as above without taking up the pen 
	}
	
	
	private void printA() {
		/*	__
		 * |__|
		 * |  |
		 * 
		 */
		
		// 5 e 10 sono i valori di LETTER_X e LETTER_Y
		// moveInsideLetter(0, 10);
		// lineInsideLetter(0, 0);
		// lineInsideLetter(5, 0);
		// lineInsideLetter(5, 10);
		// moveInsideLetter(0, 5);
		// lineInsideLetter(5, 5);
	}
	
	private void printB() {
		
	}
}
