package ch.obermuhlner.planetgen.javafx.viewer;

public class ColorUtil {

	public static javafx.scene.paint.Color toJavafxColor(ch.obermuhlner.planetgen.math.Color color) {
		return new javafx.scene.paint.Color(color.getRed(), color.getGreen(), color.getBlue(), 1);
	}
}
