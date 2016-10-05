package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.NoiseHeight;
import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.util.Units;

public class IceLayer implements Layer {

	private double temperatureOptimum = Units.celsiusToKelvin(-40);
	private double temperatureDeviation = 15;

	private final double oceanIceThickness = 100; // m 
	private final double lowGroundIceThickness = 2000; // m
	private final double highGroundIceThickness = 3; // m

	private final double maxOceanDepth = 1000; // m
	
	private final double groundOceanLevelTransitionHeight = 1000; // m	

	private final double transparentIceThickness = 2; // m
	
	private final Color iceColor;
	private NoiseHeight noiseHeight;
	

	public IceLayer(Color iceColor, NoiseHeight noiseHeight) {
		this.iceColor = iceColor;
		this.noiseHeight = noiseHeight;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		double ice = 1.0 - MathUtil.smoothstep(temperatureOptimum, temperatureOptimum + temperatureDeviation, planetPoint.temperatureAverage);
		
		double oceanDepth = planetPoint.groundHeight < 0 ? -planetPoint.groundHeight : 0;
		double oceanRelativeDepth = 1.0 - MathUtil.smoothstep(0.0, 1.0, oceanDepth / maxOceanDepth) * 0.5;
		double oceanDepthInfluence = MathUtil.smoothstep(0.90, 0.91, oceanRelativeDepth);
		double oceanIceHeight = ice * oceanIceThickness * oceanDepthInfluence;
		
		double iceHeight;
		if (planetPoint.isWater) {
			iceHeight = oceanIceHeight;
		} else {
			double groundRelativeHeight = MathUtil.smoothstep(0, 1, planetPoint.height / planet.planetData.maxHeight);
			double lowGroundTransition = MathUtil.smoothstep(0, groundOceanLevelTransitionHeight, planetPoint.height);
			double groundIceThickness = MathUtil.mix(lowGroundIceThickness, highGroundIceThickness, groundRelativeHeight);
			double groundIceHeight = ice * groundIceThickness * lowGroundTransition;
			
			iceHeight = oceanIceHeight + groundIceHeight;
		}

		if (iceHeight > 0) {
			double noise = MathUtil.smoothstep(0, 1, noiseHeight.height(latitude, longitude, context)) * 0.2 + 0.8;
			iceHeight *= noise;
			
			planetPoint.iceHeight = iceHeight;
			planetPoint.height += iceHeight;
			planetPoint.color = planetPoint.color.interpolate(iceColor, MathUtil.smoothstep(0, transparentIceThickness, iceHeight));			
		}

		planetPoint.iceColor = iceColor;
	}

}
