package ch.obermuhlner.planetgen.javafx.viewer;

public class ColorUtil {

	public static javafx.scene.paint.Color toJavafxColor(ch.obermuhlner.planetgen.math.Color color) {
		if (color == null) {
			return javafx.scene.paint.Color.TRANSPARENT;
		}
		return new javafx.scene.paint.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
}
