package bdltz;

import lejos.hardware.Button;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;

public class Main {
	public static void main(String[] args) {
	    System.out.println("Insert the pen.");		
	    
	    Button.waitForAnyPress();
	    
	    SimplePrinter printer = new SimplePrinter("5+4");
	    //printer.startPrinting();
	    
	    System.out.println("Finished.");
	    
	    Button.waitForAnyPress();
	}
}
