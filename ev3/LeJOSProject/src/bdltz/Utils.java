package bdltz;

import java.awt.Font;
import java.awt.Point;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;


public class Utils {
	public static Shape getShape(String text, Font font, Point from) {
	    FontRenderContext context = new FontRenderContext(null, false, false);

	    GeneralPath shape = new GeneralPath();
	    TextLayout layout = new TextLayout(text, font, context);

	    Shape outline = layout.getOutline(null);
	    shape.append(outline, true);

	    return shape;
	}
}
