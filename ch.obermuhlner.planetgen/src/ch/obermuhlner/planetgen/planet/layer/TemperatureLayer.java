package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.PlanetPhysics;
import ch.obermuhlner.planetgen.value.SphereValue;

public class TemperatureLayer implements Layer {

	private final SphereValue valueFunction;
	
	public TemperatureLayer(SphereValue valueFunction) {
		this.valueFunction = valueFunction;
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
			dailyTemperature = Math.sin(planet.planetData.dayTime + longitude + planet.planetData.dailyTemperatureOceanDelay) * planet.planetData.dailyBaseTemperatureVariation * planet.planetData.dailyTemperatureOceanFactor;
		} else {
			dailyTemperature = Math.sin(planet.planetData.dayTime + longitude + planet.planetData.dailyTemperatureGroundDelay) * planet.planetData.dailyBaseTemperatureVariation;
		}

		double noise = 0.5 + 1.0 * valueFunction.polarValue(latitude, longitude, context);
		
		double baseTemperature = planet.planetData.baseTemperature + (latitudeTemperature + heightTemperature) * noise;
		
		planetPoint.temperatureAverage = baseTemperature
				+ seasonalTemperature * planet.planetData.seasonTemperatureInfluenceToAverage
				+ dailyTemperature * planet.planetData.dailyTemperatureInfluenceToAverage;
		
		planetPoint.temperature = baseTemperature + seasonalTemperature + dailyTemperature; 
	}

}
