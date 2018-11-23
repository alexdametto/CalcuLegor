package bdltz;

import lejos.hardware.Button;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;

public class Main {
	public static void main(String[] args) {
	    System.out.println("Not implemented yes. Just created for the first commit.");		
	    
	   
	    /*Motor.C.rotate(400);
	    
	    Motor.B.rotate(-500);
	    
	    Motor.B.rotate(500);
	    
	    Motor.C.rotate(-400);*/
	    
	    Printer printer = new Printer("a");
	    
	    printer.startPrinting();
	    
	    System.out.println("Finished.");
	    
	    
	    Button.waitForAnyPress();
	}
}
