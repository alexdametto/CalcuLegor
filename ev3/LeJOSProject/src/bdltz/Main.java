package bdltz;

import java.text.DecimalFormat;

import lejos.hardware.Button;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class Main {
	public static void main(String[] args) {
	    System.out.println("Insert the pen.");		
	    	    
	    Button.waitForAnyPress();
	    
	    SimplePrinter printer = new SimplePrinter("ciaofioi");
	    printer.startPrinting();
	    
	    System.out.println("Finished.");
	    
	    Button.waitForAnyPress();
	}
}
