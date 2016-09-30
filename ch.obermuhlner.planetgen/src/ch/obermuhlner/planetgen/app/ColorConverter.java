package ch.obermuhlner.planetgen.app;

public class ColorConverter {

	public static javafx.scene.paint.Color toJavafxColor(ch.obermuhlner.planetgen.math.Color color) {
		return new javafx.scene.paint.Color(color.getRed(), color.getGreen(), color.getBlue(), 1);
	}
}
