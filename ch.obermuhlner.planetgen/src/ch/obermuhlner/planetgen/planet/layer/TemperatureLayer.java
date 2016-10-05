package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.PlanetPhysics;

public class TemperatureLayer implements Layer {

	private final Height heightFunction;
	
	private final double dailyOceanDelay = 0.5 * Math.PI;
	private final double dailyGroundDelay = 0.0;

	private final double dailyOceanFactor = 0.1;
	
	public TemperatureLayer(Height heightFunction) {
		this.heightFunction = heightFunction;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		double surfaceHeight = planet.planetData.hasOcean ? Math.max(0, planetPoint.height) : planetPoint.height;
		
		double minTemperature = Math.min(planet.planetData.temperatureOceanLevelToEndAtmosphere, planet.planetData.temperatureEquatorToPole);
		
		double heightTemperature = Math.max(minTemperature, planet.planetData.temperatureOceanLevelToEndAtmosphere * PlanetPhysics.heightToTemperatureFactor(surfaceHeight));
		double latitudeTemperature = Math.max(minTemperature, planet.planetData.temperatureEquatorToPole * PlanetPhysics.distanceEquatorToTemperatureFactor(PlanetPhysics.relativeDistanceEquator(latitude)));
		double seasonalTemperature = Math.sin(planet.planetData.season) * PlanetPhysics.distanceEquatorToTemperatureFactor(PlanetPhysics.hemisphereRelativeDistanceEquator(latitude)) * planet.planetData.seasonalBaseTemperatureVariation;
		double dailyTemperature;
		if (planetPoint.isWater) {
			dailyTemperature = Math.sin(planet.planetData.dayTime + longitude + dailyOceanDelay) * planet.planetData.dailyBaseTemperatureVariation * dailyOceanFactor;
		} else {
			dailyTemperature = Math.sin(planet.planetData.dayTime + longitude + dailyGroundDelay) * planet.planetData.dailyBaseTemperatureVariation;
		}

		double noise = 0.5 + 1.0 * heightFunction.height(latitude, longitude, context);
		
		planetPoint.temperatureAverage = planet.planetData.baseTemperature + (latitudeTemperature + heightTemperature) * noise;
		
		planetPoint.temperature = planetPoint.temperatureAverage + seasonalTemperature + dailyTemperature; 
	}

}
