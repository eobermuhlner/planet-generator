package ch.obermuhlner.planetgen.generator;

import java.util.Arrays;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.noise.FractalNoise;
import ch.obermuhlner.planetgen.planet.LayerType;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.layer.CityLayer;
import ch.obermuhlner.planetgen.planet.layer.CloudLayer;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer;
import ch.obermuhlner.planetgen.planet.layer.GroundLayer;
import ch.obermuhlner.planetgen.planet.layer.IceLayer;
import ch.obermuhlner.planetgen.planet.layer.OceanLayer;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer.PlantData;
import ch.obermuhlner.planetgen.planet.layer.PrecipitationLayer;
import ch.obermuhlner.planetgen.planet.layer.SnowLayer;
import ch.obermuhlner.planetgen.planet.layer.TemperatureLayer;
import ch.obermuhlner.planetgen.value.NoiseSphereValue;
import ch.obermuhlner.util.Random;
import ch.obermuhlner.util.Units;

public class PlanetGenerator {

	private static final int KM = 1000;
	
	public PlanetData createPlanetData(long... seed) {
		PlanetData planetData = new PlanetData();
		
		Random random = new Random(seed);
		
		planetData.seed = seed;
		planetData.radius = random.nextDouble(4000, 8000) * KM;
		planetData.minHeight = random.nextDouble(-1, -12) * KM;
		planetData.maxHeight = random.nextDouble(2, 8) * KM;
		planetData.hasOcean = true;
		planetData.craterDensity = random.nextBoolean(0.1) ? random.nextDouble() : 0.0;
		planetData.atmosphereHeight = planetData.maxHeight * 0.8;
		planetData.baseTemperature = 270 + random.nextDouble(50); // K
		planetData.seasonalBaseTemperatureVariation = 20; // K
		planetData.dailyBaseTemperatureVariation = 5; // K
		planetData.temperatureOceanLevelToEndAtmosphere = -50.0; // K/m
		planetData.temperatureEquatorToPole = -40.0; // K
		planetData.seasonTemperatureInfluenceToAverage = 0.2;
		planetData.dailyTemperatureInfluenceToAverage = 0.1;
		planetData.dailyTemperatureOceanDelay = 0.5 * Math.PI;
		planetData.dailyTemperatureGroundDelay = 0.0;
		planetData.dailyTemperatureOceanFactor = 0.1;
		planetData.season = random.nextDouble(2 * Math.PI);
		planetData.dayTime = random.nextDouble(2 * Math.PI);

		planetData.plants = Arrays.asList(
				PlantData.of("Tundra", 0.05, 0.05, 0.3, Units.celsiusToKelvin(-5), 10, 10, 0, Color.YELLOWGREEN),
				PlantData.of("Grass", 0.2, 0.2, 0.3, Units.celsiusToKelvin(15), 20, 20, 0, Color.LAWNGREEN),
				PlantData.of("Savanna", 0.05, 0.05, 0.3, Units.celsiusToKelvin(30), 20, 20, 0, Color.YELLOW),
				PlantData.of("Arctic rainforest", 0.5, 0.5, 1.0, Units.celsiusToKelvin(5), 10, 10, -1, Color.SEAGREEN),
				PlantData.of("Arctic forest", 0.04, 0.04, 0.4, Units.celsiusToKelvin(5), 10, 10, -1, Color.DARKSEAGREEN),
				PlantData.of("Temperate forest", 0.03, 0.03, 0.5, Units.celsiusToKelvin(10), 10, 10, 0, Color.DARKGREEN),
				PlantData.of("Temperate rainforest", 1.0, 1.0, 1.0, Units.celsiusToKelvin(10), 10, 10, -1, Color.GREEN),
				PlantData.of("Sub-tropical rainforest", 1.0, 1.0, 2.0, Units.celsiusToKelvin(20), 15, 15, -1, Color.DARKOLIVEGREEN),
				PlantData.of("Tropical rainforest", 1.0, 1.0, 2.0, Units.celsiusToKelvin(30), 20, 20, -1, Color.FORESTGREEN));
		
		return planetData;
	}
	
	public Planet createPlanet(PlanetData planetData, Random random) {
		return createEarth(planetData, random);
	}

	public Planet createEarth(PlanetData planetData, Random random) {
		Planet planet = new Planet();
		planet.planetData = planetData;

		planet.layers.put(LayerType.GROUND, new GroundLayer(
				Color.BISQUE.interpolate(Color.BEIGE, random.nextDouble()),
				Color.BEIGE.interpolate(Color.CORAL, random.nextDouble()),
				Color.BEIGE.interpolate(Color.BURLYWOOD, random.nextDouble()),
				Color.SADDLEBROWN.interpolate(Color.BISQUE, random.nextDouble()),
				Color.DARKGREY.interpolate(Color.LIGHTGREY, random.nextDouble()),
				new NoiseSphereValue(
						new FractalNoise(
							Planet.RANGE_LATITUDE * (random.nextDouble(0.2, 0.8)),
							Planet.RANGE_LATITUDE * 0.000001,
							noise -> noise,
							new FractalNoise.WeightedAmplitude(),
							random),
						planetData.minHeight,
						planetData.maxHeight)));
		planet.layers.put(LayerType.CRATERS, new CraterLayer(
				new NoiseSphereValue(
						new FractalNoise(
							Planet.RANGE_LATITUDE * 0.001,
							Planet.RANGE_LATITUDE * 0.000001,
							noise -> noise,
							new FractalNoise.PersistenceAmplitude(0.5),
							random),
						-1.0,
						1.0),
				new NoiseSphereValue(
						new FractalNoise(
							2*Math.PI * 0.1,
							2*Math.PI * 0.0001,
							noise -> noise,
							new FractalNoise.PersistenceAmplitude(0.5),
							random),
						0.0,
						1.0)));				
		planet.layers.put(LayerType.OCEAN, new OceanLayer(
				Color.DARKBLUE.interpolate(Color.BLUE, random.nextDouble())));
		planet.layers.put(LayerType.TEMPERATURE, new TemperatureLayer(
				new NoiseSphereValue(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.5,
								Planet.RANGE_LATITUDE * 0.01,
								noise -> noise,
								new FractalNoise.PersistenceAmplitude(random.nextDouble(0.3, 0.5)),
								random),
						0.0,
						1.0)));
//		planet.layers.put(LayerType.PREVAILING_WIND, new PrevailingWindLayer());
//		planet.layers.put(LayerType.PRECIPITATION, new SimulatedPrecipitationLayer());
		planet.layers.put(LayerType.PRECIPITATION, new PrecipitationLayer(
				-15, //K
				new NoiseSphereValue(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.5,
								Planet.RANGE_LATITUDE * 0.01,
								noise -> noise,
								new FractalNoise.PersistenceAmplitude(random.nextDouble(0.3, 0.5)),
								random),
						0.0,
						1.0),
				new NoiseSphereValue(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.1,
								Planet.RANGE_LATITUDE * 0.001,
								noise -> noise,
								new FractalNoise.PersistenceAmplitude(random.nextDouble(0.3, 0.5)),
								random),
						0.0,
						1.0),
				-5, //K
				new NoiseSphereValue(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.5,
								Planet.RANGE_LATITUDE * 0.01,
								noise -> noise,
								new FractalNoise.PersistenceAmplitude(random.nextDouble(0.5, 0.7)),
								random),
						0.0,
						1.0),
				new NoiseSphereValue(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.1,
								Planet.RANGE_LATITUDE * 0.001,
								noise -> noise,
								new FractalNoise.PersistenceAmplitude(0.5),
								random),
						0.0,
						1.0)));
		planet.layers.put(LayerType.ICE, new IceLayer(
				Color.WHITE,
				new NoiseSphereValue(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.1,
								Planet.RANGE_LATITUDE * 0.0001,
								noise -> noise,
								new FractalNoise.PersistenceAmplitude(random.nextDouble(0.3, 0.5)),
								random),
						0.0,
						1.0)));
		planet.layers.put(LayerType.PLANTS, new PlantLayer(
				planetData.plants));
		planet.layers.put(LayerType.SNOW, new SnowLayer(
				Color.SNOW));
		planet.layers.put(LayerType.CITIES, new CityLayer(
				Color.DARKGRAY.interpolate(Color.GRAY, random.nextDouble()),
				Color.GOLD.interpolate(Color.AQUAMARINE, random.nextDouble()),
				new NoiseSphereValue(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.1,
								Planet.RANGE_LATITUDE * 0.01,
								noise -> noise > 0 ? noise * noise : noise,
								new FractalNoise.WeightedAmplitude(),
								random),
						0.0,
						1.0)));
		planet.layers.put(LayerType.CLOUDS, new CloudLayer(
				Color.WHITE,
				new NoiseSphereValue(
						new FractalNoise(
							Planet.MAX_LONGITUDE * 0.2,
							Planet.MAX_LONGITUDE * 0.01,
							noise -> noise,
							(amplitude, noise) -> amplitude * 0.5,
							random),
						0.0,
						1.0)));
		
		return planet;
	}
}
