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
	private double currentX = 0;
	private double currentY = 0;
	
	private double currentZ = 0; // da cambiare con il grado

	private int charForRow = (int) Math.floor(( PAPER_MAX_X - 2 * DELAY_X ) / (LETTER_MAX_X + DELAY_X));
	//private int indexInRow = charForRow/2;
	private int indexInRow = 0;
	
	private int numberRow = (int) Math.floor(( PAPER_MAX_Y - 2 * DELAY_Y ) / (LETTER_MAX_Y + DELAY_Y)) ;
	private int indexRow = 0;
	
	// le velocita� son diverse, controllare con test, necessitano di una rotazione di degreePerX per fare 1 cm di movimento nell'asse X
	private double degreePerX = 111.111111;
	private double degreePerY = 90.909090;
	
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
					indexInRow = 0;
				}
				if(indexRow > numberRow) {
					
					indexRow = 0;
					indexInRow = 0;					
					// finito il foglio
					// mando fuori questo foglio
					// aspetto il prossimo foglio
					
					// ASPETTO!!!!!!!!!!!!
				}
				
				simpleMove(0, 0);
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
			case '.' :
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
				
		}
	}
	
	private double getGlobalX(double x) {
		return x + indexInRow * (LETTER_MAX_X + DELAY_X);
	}
	
	private double getGlobalY(double y) {
		return y + indexRow * (LETTER_MAX_Y + DELAY_Y);
	}

	
	private void simpleMove(final double destX, final double destY) {		
		final double dx = Math.abs(getGlobalX(currentX) - getGlobalX(destX));
		final double dy = Math.abs(getGlobalY(currentY) - getGlobalY(destY));
		
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
		    		 Motor.A.rotate((int)Math.round(degreePerX * (getGlobalX(destX) - getGlobalX(currentX)))); // il contrario forse?
		     }
		});
		
		Thread t2 = new Thread(new Runnable() {
		     @Override
		     public void run() {  
		    	 if(dy != 0)
		    		 Motor.B.rotate((int)Math.round(degreePerY * (getGlobalY(destY) - getGlobalY(currentY)))); // il contrario forse?
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
		lineInsideLetter(0,LETTER_MAX_Y);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.25);
		lineInsideLetter(0,LETTER_MAX_Y * 0.5);
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y * 0.75);
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
		moveInsideLetter(0, LETTER_MAX_Y * 0.5)
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
		lineInsideLetter();
	}
	
	private void printI() {
		
	}
	
	private void printJ() {
		
	}
	
	private void printK() {
		
	}
	
	private void printL() {
		
	}
	
	private void printM() {
		
	}
	
	private void printN() {
		
	}
	
	private void printO() {
		
	}
	
	private void printP() {
		
	}
	
	private void printQ() {
		
	}
	
	private void printR() {
		
	}
	
	private void printS() {
		
	}
	
	private void printT() {
		
	}
	
	private void printU() {
		
	}
	
	private void printV() {
		
	}
	
	private void printX() {
		
	}
	
	private void printW() {
		
	}
	
	private void printY() {
		
	}
	
	private void printZ() {
		
	}
	
	private void print0() {
		
	}
	
	private void print1() {
		
	}
	
	private void print2() {
		
	}
	
	private void print3() {
		
	}
	
	private void print4() {
		
	}
	
	private void print5() {
		
	}
	
	private void print6() {
		
	}
	
	private void print7() {
		
	}
	
	private void print8() {
		
	}
	
	private void print9() {
		
	}
	
	private void printSomma() {
		
	}
	
	private void printSottrazione() {
		
	}
	
	private void printMoltiplicazione() {
		
	}
	
	private void printDivisione() {
		
	}
	
	private void printPotenza() {
		
	}
	
	private void printUguale() {
		
	}
	
	private void printRadice() {
		
	}
	
	private void printATonda() {
		
	}
	
	private void printCTonda() {
		
	}
	
	private void printAQuadra() {
		
	}
	
	private void printCQuadra() {
		
	}
	
	private void printAGraffa() {
		
	}
	
	private void printCGraffa() {
		
	}
	
}
