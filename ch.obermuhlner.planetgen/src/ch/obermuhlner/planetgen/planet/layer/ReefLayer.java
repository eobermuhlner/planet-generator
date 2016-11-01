package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.SphereValue;
import ch.obermuhlner.util.Units;

public class ReefLayer implements Layer {

	private static final double depthOptimum = 2; //m
	private static final double depthMinusDeviation = 1; //m
	private static final double depthPlusDeviation = 500; //m
	
	private static final double maxReefHeight = depthPlusDeviation + depthMinusDeviation;
	
	private static final double temperatureOptimum = Units.celsiusToKelvin(25);
	private static final double temperatureMinusDeviation = 10;
	private static final double temperaturePlusDeviation = 20;
	
	private final Color reefColor;
	private final SphereValue valueFunction;

	public ReefLayer(Color reefColor, SphereValue valueFunction) {
		this.reefColor = reefColor;
		this.valueFunction = valueFunction;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		if (planetPoint.groundHeight < 0) {
			double depth = -planetPoint.groundHeight;
			
			double reefNoise = valueFunction.sphereValue(latitude, longitude, context);
			double temperatureDistance = MathUtil.deviationDistance(planetPoint.temperatureAverage, temperatureOptimum, temperatureMinusDeviation, temperaturePlusDeviation);
			double depthDistance = MathUtil.deviationDistance(depth, depthOptimum, depthMinusDeviation, depthPlusDeviation);

			double reefValue = reefNoise;

			reefValue *= 1.0 - MathUtil.smoothstep(0, 1, temperatureDistance);
			reefValue *= 1.0 - MathUtil.smoothstep(0, 1, depthDistance);
			reefValue = MathUtil.smoothstep(0.4, 0.5, reefValue);
			planetPoint.debug = depthDistance;
			
			planetPoint.groundHeight += reefValue * Math.min(depth, maxReefHeight);
			planetPoint.color = planetPoint.color.interpolate(reefColor, reefValue);
		}
	}

}
