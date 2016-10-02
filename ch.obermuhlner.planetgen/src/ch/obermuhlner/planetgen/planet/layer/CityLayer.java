package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.PlanetPhysics;
import ch.obermuhlner.planetgen.math.Color;

public class CityLayer implements Layer {

	public final double temperatureOptimum = PlanetPhysics.celsiusToKelvin(15);
	public final double temperatureDeviation = 20; // K
	public final double temperatureInfluence = 4;
	
	private final Color cityGroundColor;
	private final Color cityLightColor;
	private final Height heightFunction;
	
	public CityLayer(Color cityGroundColor, Color cityLightColor, Height heightFunction) {
		this.cityGroundColor = cityGroundColor;
		this.cityLightColor = cityLightColor;
		this.heightFunction = heightFunction;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context) {
		if (planetPoint.isWater || planetPoint.iceHeight > 0) {
			planetPoint.city = 0;
			return;
		}

		double noise = MathUtil.smoothstep(0.0, 1.0, heightFunction.height(latitude, longitude, context));
		double distance = Math.abs(planetData.baseTemperature - temperatureOptimum) / temperatureDeviation;
		double city = 1.0 - MathUtil.smoothstep(0, 1, distance);
		if (planetData.hasOcean) {
			double distanceToOcean = MathUtil.smoothstep(0, 1000, planetPoint.height); 
			city *= 1 - distanceToOcean;
		} else {
			city *= 0.25;
		}
		city *= noise;

		planetPoint.temperature += city * temperatureInfluence;
		
		planetPoint.color = planetPoint.color.interpolate(cityGroundColor, city);
		planetPoint.luminousColor = planetPoint.luminousColor.interpolate(cityLightColor, city * 0.3); 
	}

}
