package bdltz;

import java.awt.Point;

public class LineMovement implements Movement {
	private Point p1, p2;
	
	LineMovement(Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
}
