package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class SnowLayer implements Layer {

	private final double snowLevel = 0.8;
	
	@Override
	public double getHeight(double previousHeight, PlanetData planetData, double latitude, double longitude, double accuracy) {
		return previousHeight;
	}

	@Override
	public Color getColor(Color previousColor, PlanetData planetData, double height, Vector3 normals, double latitude, double longitude) {
		double distanceToEquator = Math.abs(latitude) / Planet.MAX_LATITUDE;
		double snow;
		
		if (height <= 0) {
			double relativeHeight = Math.abs(height / planetData.minHeight);
			double temperature = Math.min(2.0, distanceToEquator + 1.0 - relativeHeight) / 2;
			snow = MathUtil.smoothstep(snowLevel, snowLevel + 0.05, temperature);
		} else {
			double relativeHeight = height / planetData.maxHeight;
			double temperature = Math.min(2.0, distanceToEquator + relativeHeight * 2) / 2;
			snow = MathUtil.smoothstep(snowLevel, 1.0, temperature);
		}

		return previousColor.interpolate(Color.WHITE, snow);
	}

}
