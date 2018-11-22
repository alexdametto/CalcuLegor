package bdltz;

import java.awt.Point;

public class CubicMovement implements Movement {
	private Point p1, p2, p3, p4;
	
	CubicMovement(Point p1, Point p2, Point p3, Point p4) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.p4 = p4;
	}
}
