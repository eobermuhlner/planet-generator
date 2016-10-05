package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.util.Units;

public class SnowLayer implements Layer {

	private final Color snowColor;
	
	private double temperatureOptimum = Units.celsiusToKelvin(-20);
	private double temperatureDeviation = 20;
	
	private double maxSnowHeight = 2.0; // m
	
	public SnowLayer(Color snowColor) {
		this.snowColor = snowColor;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		if (!planetPoint.isWater) {
			double snow = 1.0 - MathUtil.smoothstep(temperatureOptimum, temperatureOptimum + temperatureDeviation, planetPoint.temperature);
			
			planetPoint.snowHeight = snow * maxSnowHeight;
			planetPoint.height += planetPoint.snowHeight;
			planetPoint.color = planetPoint.color.interpolate(snowColor, snow * 0.9);
		}
	}
}
