package ch.obermuhlner.planetgen.generator;

import java.util.Arrays;
import java.util.function.Function;

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
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.Crater;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.CraterCalculator;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.CraterFunction;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.CraterPartFunction;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.GridCartesianCraterCalculator;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.HeightCraterCalculator;
import ch.obermuhlner.planetgen.value.NoisePolarValue;
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
		
		NoiseSphereValue verticalHeightNoiseValue = new NoiseSphereValue(
				new FractalNoise(
					Planet.RANGE_LATITUDE * 0.001,
					Planet.RANGE_LATITUDE * 0.000001,
					noise -> noise,
					new FractalNoise.PersistenceAmplitude(0.5),
					random),
				-1.0,
				1.0);
		NoisePolarValue radialHeightNoiseValue = new NoisePolarValue(
				new FractalNoise(
					0.02,
					0.000001,
					noise -> noise,
					new FractalNoise.PersistenceAmplitude(0.2),
					random),
				0.0,
				1.0);
		
		Crater simpleRoundCrater = new Crater(
				"Simple Round Crater",
				new CraterFunction(
						craterPart(0.0, 0.7, d -> (d * d) * 4 - 3),
						craterPart(0.6, 1.0, d -> 1.0),
						craterPart(0.6, 1.0, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.7, d -> 0.0),
						craterPart(0.5, 1.0, d -> 0.2),
						craterPart(0.5, 1.0, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 1.0, d -> 0.0)),
				verticalHeightNoiseValue,
				radialHeightNoiseValue);
		
		Crater simpleFlatCrater = new Crater(
				"Simple Flat Crater",
				new CraterFunction(
						craterPart(0.0, 0.6, d -> -2.2),
						craterPart(0.3, 1.0, d -> 0.8),
						craterPart(0.3, 1.0, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.7, d -> 0.0),
						craterPart(0.5, 1.0, d -> 0.2),
						craterPart(0.5, 1.0, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 1.0, d -> 0.0)),
			verticalHeightNoiseValue,
			radialHeightNoiseValue);
		
		Crater complexFlatCrater = new Crater(
				"Complex Flat Crater",
				new CraterFunction(
						craterPart(0.0, 0.1, d -> -0.2),
						craterPart(0.0, 0.6, d -> -1.5),
						craterPart(0.4, 1.0, d -> 0.6),
						craterPart(0.4, 1.0, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.1, d -> 0.6),
						craterPart(0.0, 0.7, d -> 0.0),
						craterPart(0.5, 1.0, d -> 0.2),
						craterPart(0.5, 1.0, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.7, d -> 0.0),
						craterPart(0.4, 0.9, d -> 0.1),
						craterPart(0.4, 0.9, d -> 0.0)),
				verticalHeightNoiseValue,
				radialHeightNoiseValue);
		
		Crater complexStepsCrater = new Crater(
				"Complex Steps Crater",
				new CraterFunction(
						craterPart(0.00, 0.10, d -> -0.3),
						craterPart(0.00, 0.53, d -> -0.8),
						craterPart(0.50, 0.63, d -> -0.4),
						craterPart(0.60, 0.73, d -> 0.0),
						craterPart(0.7, 1.0, d -> 0.4),
						craterPart(0.7, 1.0, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.1, d -> 0.8),
						craterPart(0.0, 0.7, d -> 0.0),
						craterPart(0.5, 1.0, d -> 0.2),
						craterPart(0.5, 1.0, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.7, d -> 0.0),
						craterPart(0.4, 0.9, d -> 0.2),
						craterPart(0.4, 0.9, d -> 0.0)),
				verticalHeightNoiseValue,
				radialHeightNoiseValue);

		Crater simpleVolcano = new Crater(
				"Simple Volcano",
				new CraterFunction(
						craterPart(0.0, 0.2, d -> 1.0),
						craterPart(0.1, 0.6, d -> 1.0),
						craterPart(0.1, 0.6, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.2, d -> 0.1),
						craterPart(0.2, 0.3, d -> 0.1),
						craterPart(0.2, 0.3, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.9, d -> 0.2),
						craterPart(0.3, 1.0, d -> 0.0)),
				verticalHeightNoiseValue,
				radialHeightNoiseValue);

		planetData.craters = Arrays.asList(simpleRoundCrater, simpleFlatCrater, complexFlatCrater, complexStepsCrater, simpleVolcano);

		double baseHeight = 2000;
		planetData.craterCalculators = Arrays.asList(
				createCraterCalculator(baseHeight,   13, simpleVolcano),

				createCraterCalculator(baseHeight,   5, complexStepsCrater),
				createCraterCalculator(baseHeight,   7, complexStepsCrater),
				createCraterCalculator(baseHeight,   11, complexFlatCrater),
				createCraterCalculator(baseHeight,   17, complexFlatCrater),
				createCraterCalculator(baseHeight,   23, complexFlatCrater),
				createCraterCalculator(baseHeight,   31, simpleFlatCrater),
				createCraterCalculator(baseHeight,   51, simpleFlatCrater),
				createCraterCalculator(baseHeight,   79, simpleFlatCrater),
				createCraterCalculator(baseHeight,  113, simpleRoundCrater),
				createCraterCalculator(baseHeight,  201, simpleRoundCrater),
				createCraterCalculator(baseHeight,  471, simpleRoundCrater),
				createCraterCalculator(baseHeight,  693, simpleRoundCrater),
				createCraterCalculator(baseHeight,  877, simpleRoundCrater),
				createCraterCalculator(baseHeight, 1003, simpleRoundCrater),
				createCraterCalculator(baseHeight, 1301, simpleRoundCrater),
				createCraterCalculator(baseHeight, 1707, simpleRoundCrater)
		);
		
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
				planetData.craterCalculators));				
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

	private static CraterCalculator createCraterCalculator(double baseHeight, int grid, Crater crater) {
		return new HeightCraterCalculator(baseHeight / grid, new GridCartesianCraterCalculator(grid, crater));
	}
	
	private static CraterPartFunction craterPart(double minDist, double maxDist, Function<Double, Double> func) {
		return new CraterPartFunction(minDist, maxDist, func);
	}
}
