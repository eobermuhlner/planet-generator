package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.PlanetPhysics;

public class TemperatureLayer implements Layer {

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context) {
		double surfaceHeight = planetData.hasOcean ? Math.max(0, planetPoint.height) : planetPoint.height;
		
		double minTemperature = Math.min(planetData.temperatureOceanLevelToEndAtmosphere, planetData.temperatureEquatorToPole);
		
		double heightTemperature = Math.max(minTemperature, planetData.temperatureOceanLevelToEndAtmosphere * PlanetPhysics.heightToTemperatureFactor(surfaceHeight));
		double latitudeTemperature = Math.max(minTemperature, planetData.temperatureEquatorToPole * PlanetPhysics.distanceEquatorToTemperatureFactor(PlanetPhysics.relativeDistanceEquator(latitude)));
		double seasonalTemperature = Math.sin(planetData.season) * PlanetPhysics.distanceEquatorToTemperatureFactor(PlanetPhysics.hemisphereRelativeDistanceEquator(latitude)) * planetData.seasonalBaseTemperature;
		planetPoint.temperature = 
				planetData.baseTemperature
				+ latitudeTemperature
				+ heightTemperature
				+ seasonalTemperature;
	}

}
