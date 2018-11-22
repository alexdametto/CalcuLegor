package bdltz;

import java.awt.Font;
import java.awt.Point;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

public class Printer {
	private ArrayList<String> container;
	
	Printer(ArrayList<String> toPrint) {
		this.container = toPrint;
	}
	
	Printer(String toPrint) {
		this.container = new ArrayList<String>();
		container.add(toPrint);
	}
	
	public void startPrinting() {
		Font font = new Font("Times new Roman", Font.BOLD, 12);
		for(String passo : container) {
			Point p = new Point(0, 0);
			Shape shape = Utils.getShape(passo, font, p);
			
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
		            x3 = x2;
		            y3 = y2;

		            x2 = x1 + 1 / 3f * (x2 - x1);
		            y2 = y1 + 1 / 3f * (y2 - y1);

		            x1 = x + 2 / 3f * (x1 - x);
		            y1 = y + 2 / 3f * (y1 - y);

		            x = x3;
		            y = y3;
		            break;

		        case PathIterator.SEG_CUBICTO:
		            x = x3;
		            y = y3;
		            break;
		        case PathIterator.SEG_LINETO:
		            x = x1;
		            y = y1;
		            break;
		        case PathIterator.SEG_MOVETO:
		            x = x1;
		            y = y1;
		            break;
		        }
		        iterator.next();
		    }
		}
		
		// FINITO!!!!
	}
}



/*
 * 
 * 
 * 
 * 
 * 
 * 
 * 
public List<Point> getPoints(Shape shape) {
    List<Point> out = new ArrayList<Point>();
    PathIterator iterator = shape.getPathIterator(null);

    double[] coordinates = new double[6];
    double x = 0, y = 0;

    while (!iterator.isDone()) {

        double x1 = coordinates[0];
        double y1 = coordinates[1];

        double x2 = coordinates[2];
        double y2 = coordinates[3];

        double x3 = coordinates[4];
        double y3 = coordinates[5];

        switch (iterator.currentSegment(coordinates)) {
        case PathIterator.SEG_QUADTO:
            x3 = x2;
            y3 = y2;

            x2 = x1 + 1 / 3f * (x2 - x1);
            y2 = y1 + 1 / 3f * (y2 - y1);

            x1 = x + 2 / 3f * (x1 - x);
            y1 = y + 2 / 3f * (y1 - y);

            out.add(new Point(x3, y3));

            x = x3;
            y = y3;
            break;

        case PathIterator.SEG_CUBICTO:
            out.add(new Point(x3, y3));
            x = x3;
            y = y3;
            break;
        case PathIterator.SEG_LINETO:
            out.add(new Point(x1, y1));
            x = x1;
            y = y1;
            break;
        case PathIterator.SEG_MOVETO:
            out.add(new Point(x1, y1));
            x = x1;
            y = y1;
            break;
        }
        iterator.next();
    }

    return out;
}
 * 
 * 
 * 
 * */