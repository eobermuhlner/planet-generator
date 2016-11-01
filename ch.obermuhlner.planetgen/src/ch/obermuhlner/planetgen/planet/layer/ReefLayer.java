package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.SphereValue;
import ch.obermuhlner.util.Units;

public class ReefLayer implements Layer {

	private static final double depthOptimum = 10; //m
	private static final double depthMinusDeviation = 200; //m
	private static final double depthPlusDeviation = 9; //m
	
	private static final double maxReefHeight = depthPlusDeviation + depthMinusDeviation;
	
	private static final double temperatureOptimum = Units.celsiusToKelvin(25);
	private static final double temperatureMinusDeviation = Units.celsiusToKelvin(5);
	private static final double temperaturePlusDeviation = Units.celsiusToKelvin(5);
	
	private final Color reefColor;
	private final SphereValue valueFunction;

	public ReefLayer(Color reefColor, SphereValue valueFunction) {
		this.reefColor = reefColor;
		this.valueFunction = valueFunction;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		if (planetPoint.groundHeight < 0) {
			double reefNoise = valueFunction.sphereValue(latitude, longitude, context);
			double temperatureDistance = MathUtil.deviationDistance(planetPoint.temperatureAverage, temperatureOptimum, temperatureMinusDeviation, temperaturePlusDeviation);
			double depthDistance = MathUtil.deviationDistance(-planetPoint.groundHeight, depthOptimum, depthMinusDeviation, depthPlusDeviation);

			double reefValue = reefNoise;
			reefValue *= 1.0 - MathUtil.smoothstep(0, 1, temperatureDistance);
			reefValue *= 1.0 - MathUtil.smoothstep(0, 1, depthDistance);
			
			planetPoint.groundHeight += reefValue * maxReefHeight;
			planetPoint.color = planetPoint.color.interpolate(reefColor, reefValue);
		}
	}

}
