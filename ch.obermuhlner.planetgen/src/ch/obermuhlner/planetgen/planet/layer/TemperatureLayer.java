package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.planet.PlanetData;
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
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context) {
		double surfaceHeight = planetData.hasOcean ? Math.max(0, planetPoint.height) : planetPoint.height;
		
		double minTemperature = Math.min(planetData.temperatureOceanLevelToEndAtmosphere, planetData.temperatureEquatorToPole);
		
		double heightTemperature = Math.max(minTemperature, planetData.temperatureOceanLevelToEndAtmosphere * PlanetPhysics.heightToTemperatureFactor(surfaceHeight));
		double latitudeTemperature = Math.max(minTemperature, planetData.temperatureEquatorToPole * PlanetPhysics.distanceEquatorToTemperatureFactor(PlanetPhysics.relativeDistanceEquator(latitude)));
		double seasonalTemperature = Math.sin(planetData.season) * PlanetPhysics.distanceEquatorToTemperatureFactor(PlanetPhysics.hemisphereRelativeDistanceEquator(latitude)) * planetData.seasonalBaseTemperatureVariation;
		double dailyTemperature;
		if (planetPoint.isWater) {
			dailyTemperature = Math.sin(planetData.dayTime + longitude + dailyOceanDelay) * planetData.dailyBaseTemperatureVariation * dailyOceanFactor;
		} else {
			dailyTemperature = Math.sin(planetData.dayTime + longitude + dailyGroundDelay) * planetData.dailyBaseTemperatureVariation;
		}

		double noise = 0.5 + 1.0 * heightFunction.height(latitude, longitude, context);
		
		planetPoint.temperatureAverage = planetData.baseTemperature + (latitudeTemperature + heightTemperature) * noise;
		
		planetPoint.temperature = planetPoint.temperatureAverage + seasonalTemperature + dailyTemperature; 
	}

}
