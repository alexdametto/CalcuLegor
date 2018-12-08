package bdltz;

import lejos.hardware.Button;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;

public class Main {
	public static void main(String[] args) {
		
		Motor.C.rotate(800);
		
	    System.out.println("Insert the pen.");		
	    
	    Button.waitForAnyPress();
	    
	    Motor.C.rotate(-800);
	   
	    /*Motor.C.rotate(400);
	    
	    Motor.B.rotate(-500);
	    
	    Motor.B.rotate(500);
	    
	    Motor.C.rotate(-400);*/
	    
	    
	    SimplePrinter printer = new SimplePrinter("BDLTZ");
	    
	    //Motor.C.rotate(-1000);
	    
	    //Motor.B.rotate(500);
	    
	    printer.startPrinting();
	    
	    //Motor.A.rotate(500)
	    
	    System.out.println("Finished.");
	    
	    
	    Button.waitForAnyPress();
	}
}
