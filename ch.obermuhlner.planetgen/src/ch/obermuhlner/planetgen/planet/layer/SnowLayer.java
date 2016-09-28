package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class SnowLayer implements Layer {

	private final Color snowColor;
	private final double snowLevel = 0.9;
	
	public SnowLayer(Color snowColor) {
		this.snowColor = snowColor;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, double accuracy) {
		if (planetPoint.height > 0) {
			double distanceToEquator = relativeDistanceToEquator(latitude);
			double relativeHeight = planetPoint.height / planetData.maxHeight;
			double temperature = distanceToEquator + relativeHeight * 0.9;
			double seasonEffect = 1.0 - latitude / Planet.MAX_LATITUDE;
			temperature = temperature * seasonEffect;
			double snow = MathUtil.smoothstep(snowLevel * 0.6, snowLevel, temperature);

			planetPoint.color = planetPoint.color.interpolate(snowColor, snow * 0.8);
		}
	}
}
