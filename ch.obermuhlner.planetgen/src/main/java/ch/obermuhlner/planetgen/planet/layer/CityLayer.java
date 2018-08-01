package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.SphereValue;
import ch.obermuhlner.util.Units;

public class CityLayer implements Layer {

	public final double temperatureOptimum = Units.celsiusToKelvin(15);
	public final double temperatureDeviation = 20; // K
	public final double temperatureInfluence = 4;
	
	private final Color cityGroundColor;
	private final Color cityLightColor;
	private final SphereValue valueFunction;
	
	public CityLayer(Color cityGroundColor, Color cityLightColor, SphereValue valueFunction) {
		this.cityGroundColor = cityGroundColor;
		this.cityLightColor = cityLightColor;
		this.valueFunction = valueFunction;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		if (planetPoint.isWater || planetPoint.iceHeight > 0) {
			planetPoint.city = 0;
			return;
		}

		double noise = MathUtil.smoothstep(0.0, 1.0, valueFunction.sphereValue(latitude, longitude, context));
		double distance = Math.abs(planet.planetData.baseTemperature - temperatureOptimum) / temperatureDeviation;
		double city = 1.0 - MathUtil.smoothstep(0, 1, distance);
		if (planet.planetData.hasOcean) {
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
