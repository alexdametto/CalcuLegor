package bdltz;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import lejos.hardware.motor.Motor;

public class Printer {
	
	/*
	 * Motor.A = motore spostare asse X
	 * Motor.B = motore spostare asse Y
	 * Motor.C = motore spostare asse Z (con gradi in < 0 tiro giù la penna, con gradi in > 0 la alzo!!!!)
	 */
	
	public class Point {
		private double x = 0, y = 0;
		Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		Point() {
			this.x = 0; 
			this.y = 0;
		}
		
		public double getX() {
			return this.x;
		}
		
		public double getY() {
			return this.y;
		}
	}
	
	public class Point3D extends Point {
		private double z;
		
		Point3D(double x, double y, double z) {
			super(x, y);
			this.z = z;
		}
		
		public double getZ() {
			return this.z;
		}
	}
	
	
	// ASSUMING THAT 100 is max X for a paper.
	
	/*
	 * Default position is with x centered.
	 */
	
	private ArrayList<String> container;
	private Point3D currentPos = new Point3D(0, 0, 0);
	private int defaultSpeed = 360; // gradi al secondo (quindi tipo 360?) (da vedere)
	private int degreeUnit = 10; // 10 gradi per fare 1 unità (da vedere), probabilmente sono 3 diverse, X Y e Z
	private int zAngle = 800; // angle for take up the pen (or take down)
	
	
	Printer(ArrayList<String> toPrint) {
		this.container = toPrint;
	}
	
	Printer(String toPrint) {
		this.container = new ArrayList<String>();
		container.add(toPrint);
	}
	
	public void startPrinting() {		
		Font font = new Font("TimesRoman", Font.PLAIN, 12);
				
		for(String passo : container) {
			
			System.out.println(passo);
			
			// wait for click???
			
			// 0, 0 sono le coordinate del punto di inizio e fine!!!!
			Shape shape = Utils.getShape(passo, font, 0, 0);
			
		    double[] coordinates = new double[6];
			double x = 0, y = 0;
			
			PathIterator iterator = shape.getPathIterator(null);
			
			while (!iterator.isDone()) {

		        double x1 = coordinates[0];
		        double y1 = coordinates[1];

		        double x2 = coordinates[2];
		        double y2 = coordinates[3];

		        double x3 = coordinates[4];
		        double y3 = coordinates[5];

		        switch (iterator.currentSegment(coordinates)) {
		        case PathIterator.SEG_QUADTO:
		            QuadraticTo(new Point(x1, y1), new Point(x2, y2));

		            x = x2;
		            y = y2;
		            break;

		        case PathIterator.SEG_CUBICTO:
		        	CubicTo(new Point(x1, y1), new Point(x2, y2), new Point(x3, y3));
		        	
		            x = x3;
		            y = y3;
		            break;
		            
		        case PathIterator.SEG_LINETO:
		            LineTo(new Point(x1, y1));
		            
		            x = x1;
		            y = y1;
		            
		            break;
		            
		        case PathIterator.SEG_MOVETO:
		        	MoveTo(new Point(x1, y1));
		        	
		            x = x1;
		            y = y1;
		            break;
		        }
		        
		        this.currentPos = new Point3D(x, y, currentPos.getY());
		        
		        iterator.next();
		    }
			
			// wait for click?
		}

		// finished printing.
		 
	}
	
	private void MoveUp() {
		Motor.C.rotate(zAngle);
	}
	
	private void MoveDown() {
		Motor.C.rotate(-zAngle);
	}
	
	
	private void MoveTo(Point destination) {
		// alza e sposta la penna da dove sta ora fino a un punto destinazione
		
		// cambiare velocità

		MoveUp();
		
		simpleMove(destination);
		
		MoveDown();
	}
	
	private void LineTo(Point destination) {
		// linea da dove sta ora fino a un punto destinazione
		
		// dovrei sempre avere la penna giù quindi no problem.
		simpleMove(destination);
		
	}
	
	private void CubicTo(Point p2, Point p3, Point destination) {
		// crea una curva di bezier cubica da dove sta ora fino a destination, utilizzando p2 e p3
		
		double precision = 1; // 10 intermediate point between currentPos and destination
		
		double Dt = 1 / precision; // Delta t
						
		// first time t = Dt, not 0 because t = 0 we will evaluate currentPos.
		for( double t = Dt ; t <= 1; t = t + Dt) {
			Point intermediatePoint = BezierFunction(t, p2, p3, destination);
			LineTo(intermediatePoint);
		}
	}
	
	private void QuadraticTo(Point p2, Point destination) {
		// crea una curva di bezier quadratica da dove sta ora fino a destination, utilizzando p2
		
		double precision = 1; // 10 intermediate point between currentPos and destination
		
		double Dt = 1 / precision; // Delta t
				
		// first time t = Dt, not 0 because t = 0 we will evaluate currentPos.
		for( double t = Dt ; t <= 1; t = t + Dt) {
			Point intermediatePoint = BezierFunction(t, p2, null, destination);
			LineTo(intermediatePoint);
		}
	}
	
	
	// move from a point to a point at the same time and with the same duration
	// DOESN'T WORK, SOMETHING WRONG WITH SIGN!!!!!!!!!!!
	private void simpleMove(final Point destination) {
		final double dx = Math.abs(destination.getX() - currentPos.getX());
		final double dy = Math.abs(destination.getY() - currentPos.getY());
		
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
		    	 Motor.A.rotate((int)Math.round(degreeUnit * (destination.getX() - currentPos.getX()))); // il contrario forse?
		     }
		});
		
		Thread t2 = new Thread(new Runnable() {
		     @Override
		     public void run() {   	 
		    	 Motor.B.rotate((int)Math.round(degreeUnit * (destination.getY() - currentPos.getY()))); // il contrario forse?
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
	}
	
	
	// Bezier function
	private Point BezierFunction(double t, Point p1, Point p2, Point destination) throws IllegalArgumentException {
		if(t < 0 || t > 1) {
			throw new IllegalArgumentException("t must be 0 <= t <= 1 !!!!");
		}
		
		if(p1 == null) {
			throw new IllegalArgumentException("P1 can't be null object.");
		}
		
		if(p2 == null) {
			// we have 3 points, currentPos - p1 - destination, we can evaluate QUADRATIC BEZIER CURVE

			
			double x = Math.pow(1 - t, 2) * currentPos.getX() + 2 * t * (1 - t) * p1.getX() + Math.pow(t, 2) * destination.getX();
			double y = Math.pow(1 - t, 2) * currentPos.getY() + 2 * t * (1 - t) * p1.getY() + Math.pow(t, 2) * destination.getY();
			
			return new Point(x, y);
		}
		else {
			// we have 4 points, currentPos - p1 - p2 - destination, we can evaluate CUBIC BEZIER CURVE
			
			double x = Math.pow(1 - t, 3) * currentPos.getX() + 3 * p1.getX() * t * Math.pow(1 - t, 2) + 3 * p2.getX() + Math.pow(t, 2) * (1 - t) + destination.getX() * Math.pow(t,  3);
			double y = Math.pow(1 - t, 3) * currentPos.getY() + 3 * p1.getY() * t * Math.pow(1 - t, 2) + 3 * p2.getY() + Math.pow(t, 2) * (1 - t) + destination.getY() * Math.pow(t,  3);
			
			return new Point(x, y);
		}
	}
	
	
}