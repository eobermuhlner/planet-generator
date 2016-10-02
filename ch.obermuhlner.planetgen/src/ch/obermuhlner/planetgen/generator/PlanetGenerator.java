package ch.obermuhlner.planetgen.generator;

import java.util.Arrays;
import java.util.Random;

import ch.obermuhlner.planetgen.height.FractalNoise;
import ch.obermuhlner.planetgen.height.NoiseHeight;
import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetPhysics;
import ch.obermuhlner.planetgen.planet.layer.CityLayer;
import ch.obermuhlner.planetgen.planet.layer.GroundLayer;
import ch.obermuhlner.planetgen.planet.layer.IceLayer;
import ch.obermuhlner.planetgen.planet.layer.OceanLayer;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer;
import ch.obermuhlner.planetgen.planet.layer.PrecipitationLayer;
import ch.obermuhlner.planetgen.planet.layer.SnowLayer;
import ch.obermuhlner.planetgen.planet.layer.TemperatureLayer;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer.PlantData;

public class PlanetGenerator {

	private static final int KM = 1000;
	
	public PlanetData createPlanetData(Random random) {
		PlanetData planetData = new PlanetData();
		
		planetData.radius = random.nextDouble() * 4000 * KM + 4000 * KM;
		planetData.minHeight = random.nextDouble() * -10 * KM + -2 * KM;
		planetData.maxHeight = random.nextDouble() * 6 * KM + 2 * KM;
		planetData.hasOcean = true;
		planetData.atmosphereHeight = planetData.maxHeight * 0.8;
		planetData.baseTemperature = 270 + random.nextDouble() * 50; // K
		planetData.seasonalBaseTemperatureVariation = 20; // K
		planetData.dailyBaseTemperatureVariation = 5; // K
		planetData.temperatureOceanLevelToEndAtmosphere = -50.0; // K/m
		planetData.temperatureEquatorToPole = -40.0; // K
		planetData.season = random.nextDouble() * 2 * Math.PI;
		planetData.dayTime = 0.5 * Math.PI;

		return planetData;
	}
	
	public Planet createPlanet(PlanetData planetData, Random random) {
		return createEarth(planetData, random);
	}

	public Planet createEarth(PlanetData planetData, Random random) {
		Planet planet = new Planet();
		planet.planetData = planetData;

		double largestFeature = random.nextDouble() * 0.6 + 0.2;

		planet.layers.put("Ground", new GroundLayer(
				Color.BISQUE.interpolate(Color.BEIGE, random.nextDouble()),
				Color.BEIGE.interpolate(Color.CORAL, random.nextDouble()),
				Color.BEIGE.interpolate(Color.BURLYWOOD, random.nextDouble()),
				Color.SADDLEBROWN.interpolate(Color.BISQUE, random.nextDouble()),
				Color.DARKGREY.interpolate(Color.LIGHTGREY, random.nextDouble()),
				new NoiseHeight(
						new FractalNoise(
							Planet.RANGE_LATITUDE * largestFeature,
							Planet.RANGE_LATITUDE * 0.0001,
							noise -> noise,
							new FractalNoise.WeightedAmplitude(),
							random),
						planetData.minHeight,
						planetData.maxHeight)));
//		planet.layers.put("Craters", new CraterLayer());
		planet.layers.put("Ocean", new OceanLayer(
				Color.DARKBLUE.interpolate(Color.BLUE, random.nextDouble())));
		planet.layers.put("Temperature", new TemperatureLayer(
				new NoiseHeight(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.5,
								Planet.RANGE_LATITUDE * 0.01,
								noise -> noise,
								new FractalNoise.PersistenceAmplitude(random.nextDouble() * 0.2 + 0.3),
								random),
						0.0,
						1.0)));
		planet.layers.put("Precipitation", new PrecipitationLayer(
				-15, //K
				new NoiseHeight(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.1,
								Planet.RANGE_LATITUDE * 0.0001,
								noise -> noise,
								new FractalNoise.PersistenceAmplitude(random.nextDouble() * 0.2 + 0.3),
								random),
						0.0,
						1.0)));
		planet.layers.put("Ice", new IceLayer(
				Color.WHITE,
				new NoiseHeight(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.1,
								Planet.RANGE_LATITUDE * 0.0001,
								noise -> noise,
								new FractalNoise.PersistenceAmplitude(random.nextDouble() * 0.2 + 0.3),
								random),
						0.0,
						1.0)));
		planet.layers.put("Plants", new PlantLayer(
				Arrays.asList(
						PlantData.of("tundra", 0.1, 0.1, PlanetPhysics.celsiusToKelvin(-5), 10, 0, Color.YELLOWGREEN),
						PlantData.of("savanna", 0.2, 0.1, PlanetPhysics.celsiusToKelvin(30), 20, 0, Color.YELLOW),
						PlantData.of("temperate forest", 0.2, 0.1, PlanetPhysics.celsiusToKelvin(5), 20, 0, Color.DARKGREEN),
						PlantData.of("arctic rainforest", 1.0, 2.0, PlanetPhysics.celsiusToKelvin(5), 20, -1, Color.SEAGREEN),
						PlantData.of("temperate rainforest", 1.0, 2.0, PlanetPhysics.celsiusToKelvin(10), 20, -1, Color.GREEN),
						PlantData.of("sub-tropical rainforest", 1.0, 2.0, PlanetPhysics.celsiusToKelvin(20), 20, -1, Color.DARKOLIVEGREEN),
						PlantData.of("tropical rainforest", 1.0, 2.0, PlanetPhysics.celsiusToKelvin(30), 20, -1, Color.FORESTGREEN))));
		planet.layers.put("Snow", new SnowLayer(
				Color.SNOW));
		planet.layers.put("Cities", new CityLayer(
				Color.DARKGRAY.interpolate(Color.GRAY, random.nextDouble()),
				Color.GOLD.interpolate(Color.AQUAMARINE, random.nextDouble()),
				new NoiseHeight(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.1,
								Planet.RANGE_LATITUDE * 0.01,
								noise -> noise > 0 ? noise * noise : noise,
								new FractalNoise.WeightedAmplitude(),
								random),
						0.0,
						1.0)));
//		planet.layers.put("Clouds", new CloudLayer(
//				Color.WHITE,
//				new NoiseHeight(
//						new FractalNoise(
//							Planet.MAX_LONGITUDE * 0.2,
//							Planet.MAX_LONGITUDE * 0.01,
//							noise -> noise,
//							(amplitude, noise) -> amplitude * 0.5,
//							random),
//						0.0,
//						1.0)));
		
		return planet;
	}
}
