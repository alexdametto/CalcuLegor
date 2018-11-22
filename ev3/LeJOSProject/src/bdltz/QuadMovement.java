package bdltz;

import java.awt.Point;

public class QuadMovement implements Movement {
	private Point p1, p2, p3;
	
	QuadMovement(Point p1, Point p2, Point p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}
}
