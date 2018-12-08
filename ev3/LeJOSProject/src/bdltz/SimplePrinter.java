package bdltz;

import java.util.ArrayList;

import lejos.hardware.motor.Motor;

public class SimplePrinter {
	private ArrayList<String> container;
	
	// FOGLI DA 14 cm di larghezza, lunghi come un A4 (quindi 14 cm x 29.7 cm)
	//private static final double PAPER_CM_X = 14; // da rivedere, probabilmente 15 cm
	//private static final double PAPER_CM_Y = 29.7;
	
	// da rivedere
	private static final double PAPER_MAX_X = 6.5; // sono circa 6.5 cm...
	private static final double PAPER_MAX_Y = 20; 
	
	// a letter is LETTER_MAX_X * LETTER_MAX_Y rectangle.
	private static final double LETTER_MAX_X = 1; 
	private static final double LETTER_MAX_Y = 2;
	private static final double DELAY_X = 0.5;
	private static final double DELAY_Y = 0.5;
		
	// inside the letter....
	private double currentX = PAPER_MAX_X / 2;
	private double currentY = 0;
	
	private double currentZ = 0; // da cambiare con il grado

	private int charForRow = (int) Math.floor(( PAPER_MAX_X - 2 * DELAY_X ) / (LETTER_MAX_X + DELAY_X));
	//private int indexInRow = charForRow/2;
	private int indexInRow = 0;
	
	private int numberRow = (int) Math.floor(( PAPER_MAX_Y - 2 * DELAY_Y ) / (LETTER_MAX_Y + DELAY_Y)) ;
	private int indexRow = 0;
	
	// le velocitaÂ  son diverse, controllare con test, necessitano di una rotazione di degreePerX per fare 1 cm di movimento nell'asse X
	private double degreePerX = 111.111111;
	private double degreePerY = 90.909090;
	
	
	private int defaultSpeed = 360; // 720 degress per seconds
	
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
				moveInsideLetter(0, 0);
				char c = passo.charAt(i);
				
				printChar(c);
				
				System.out.println(indexRow + " , " + indexInRow);
				
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
	
	private double getGlobalX(double x) {		
		return x + (double)(indexInRow) * (LETTER_MAX_X + DELAY_X);
	}
	
	private double getGlobalY(double y) {
		return y + (double)(indexRow) * (LETTER_MAX_Y + DELAY_Y);
	}

	
	private void simpleMove(final double destX, final double destY) {		
		final double dx = Math.abs(currentX - getGlobalX(destX));
		final double dy = Math.abs(currentY - getGlobalY(destY));
				
		// We don't need a movement.
		if(dx == 0 && dy == 0)
			return;
		
		int speedDx = defaultSpeed;
		int speedDy = defaultSpeed;
		
		if(dx > dy && dy != 0) {			
			// dy ha meno spazio da fare, dx deve velocizzarsi per fare piÃƒÂ¹ spazio in meno tempo.
			// vel = spazio / tempo 	=> tempo = spazio / vel
			
			// tempoDx = tempoDy 		=> spazioDx / velDx = spazioDy / velDy
			speedDx = (int) Math.round(speedDy / dy * dx);
		}
		else if(dx < dy && dx != 0) {
			// dx ha meno spazio da fare, dy deve velocizzarsi per fare piÃƒÂ¹ spazio in meno tempo.
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
			t1.join();
			t2.join();	
			
			currentX = getGlobalX(destX);
			currentY = getGlobalY(destY);
			
			// wait both thread before going out.
		}catch(Exception e) {
			System.out.println("Error executing parallel motors move. Contact the productor.");
		}
	}
	
	
	// move from a point to a point at the same time and with the same duration
	// DOESN'T WORK, SOMETHING WRONG WITH SIGNS!!!!!!!!!!!
	private void moveInsideLetter(double destX, double destY) {
		// alza il motore se serve
		
		Motor.C.rotate(200);
				
		simpleMove(destX, destY);
		
		Motor.C.rotate(-200);
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
		lineInsideLetter(LETTER_MAX_X, LETTER_MAX_Y);
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
