package ch.obermuhlner.planetgen.awt;

public class ColorUtil {

	private static java.awt.Color AWT_TRANSPARENT = new java.awt.Color(0, 0, 0, 0);

	public static java.awt.Color toAwtColor(ch.obermuhlner.planetgen.math.Color color) {
		if (color == null) {
			return AWT_TRANSPARENT;
		}
		return new java.awt.Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getAlpha());
	}
}
