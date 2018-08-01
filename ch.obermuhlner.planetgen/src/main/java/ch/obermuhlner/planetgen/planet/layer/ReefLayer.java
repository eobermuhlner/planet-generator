package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.SphereValue;
import ch.obermuhlner.util.Units;

public class ReefLayer implements Layer {

	private static final double depthOptimum = 1; //m
	private static final double depthMinusDeviation = 1; //m
	private static final double depthPlusMinDeviation = 50; //m
	private static final double depthPlusMaxDeviation = 5000; //m
	
	private static final double temperatureOptimum = Units.celsiusToKelvin(25);
	private static final double temperatureMinusDeviation = 10;
	private static final double temperaturePlusDeviation = 20;
	
	private final Color reefColor;
	private final SphereValue reefValueFunction;
	private final SphereValue reefHeightValueFunction;

	public ReefLayer(Color reefColor, SphereValue reefValueFunction, SphereValue reefHeightValueFunction) {
		this.reefColor = reefColor;
		this.reefValueFunction = reefValueFunction;
		this.reefHeightValueFunction = reefHeightValueFunction;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		if (planetPoint.groundHeight < 0) {
			double depth = -planetPoint.groundHeight;
			
			double reefNoise = MathUtil.smoothstep(0, 1, reefValueFunction.sphereValue(latitude, longitude, context));
			
			double depthPlusDeviation = MathUtil.mix(depthPlusMinDeviation, depthPlusMaxDeviation, reefNoise*reefNoise);
			double depthDistance = MathUtil.deviationDistance(depth, depthOptimum, depthMinusDeviation, depthPlusDeviation);
			double temperatureDistance = MathUtil.deviationDistance(planetPoint.temperatureAverage, temperatureOptimum, temperatureMinusDeviation, temperaturePlusDeviation);

			double reefValue = reefNoise;
			reefValue *= 1.0 - MathUtil.smoothstep(0, 1, temperatureDistance);
			reefValue *= 1.0 - MathUtil.smoothstep(0, 1, depthDistance);
			reefValue = MathUtil.smoothstep(0.6, 0.8, reefValue);

			if (reefValue > 0) {
				double maxReefHeight = depthMinusDeviation + depthPlusDeviation;
				double reefHeightNoise = MathUtil.smoothstep(0.5, 1.0, reefHeightValueFunction.sphereValue(latitude, longitude, context));
				reefValue *= reefHeightNoise;
				planetPoint.reefHeight = reefValue * Math.min(depth, maxReefHeight);
				planetPoint.groundHeight += planetPoint.reefHeight;
				planetPoint.height = planetPoint.groundHeight;
				planetPoint.color = planetPoint.color.interpolate(reefColor, reefValue);
			}
		}
	}

}
