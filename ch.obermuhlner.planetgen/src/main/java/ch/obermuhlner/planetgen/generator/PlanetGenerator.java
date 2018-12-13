package ch.obermuhlner.planetgen.generator;

import java.util.Arrays;
import java.util.function.Function;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.RandomColor;
import ch.obermuhlner.planetgen.noise.FractalNoise;
import ch.obermuhlner.planetgen.planet.LayerType;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.layer.AtmosphericPressureLayer;
import ch.obermuhlner.planetgen.planet.layer.CityLayer;
import ch.obermuhlner.planetgen.planet.layer.CloudLayer;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.Crater;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.CraterCalculator;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.CraterFunction;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.CraterPartFunction;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.DensityFunction;
import ch.obermuhlner.planetgen.planet.layer.GroundLayer;
import ch.obermuhlner.planetgen.planet.layer.IceLayer;
import ch.obermuhlner.planetgen.planet.layer.OceanLayer;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer.PlantData;
import ch.obermuhlner.planetgen.planet.layer.PrecipitationLayer;
import ch.obermuhlner.planetgen.planet.layer.ReefLayer;
import ch.obermuhlner.planetgen.planet.layer.SnowLayer;
import ch.obermuhlner.planetgen.planet.layer.TemperatureLayer;
import ch.obermuhlner.planetgen.value.NoisePolarValue;
import ch.obermuhlner.planetgen.value.NoiseSphereValue;
import ch.obermuhlner.planetgen.value.NoiseVector2Value;
import ch.obermuhlner.planetgen.value.SphereValue;
import ch.obermuhlner.util.Random;
import ch.obermuhlner.util.Units;

import static ch.obermuhlner.planetgen.math.Color.rgb;

public class PlanetGenerator {

	public static final long HOURS = 60 * 60 * 1000;
	public static final long DAYS = HOURS * 24;

	public static final int KM = 1000;

	/**
	 * Creates a planets data from a seed.
	 *
	 * @param seed the random seed
	 * @return the created {@link PlanetData}
	 */
	public PlanetData createPlanetData(long... seed) {
		PlanetData planetData = new PlanetData();
		
		Random random = new Random(seed);
		
		planetData.seed = seed;
		planetData.orbitTime = (long) (random.nextDouble(100, 500) * DAYS);
		planetData.revolutionTime = (long) (random.nextBoolean(0.01) ? planetData.orbitTime : random.nextDouble(10, 40) * HOURS);
		planetData.radius = random.nextDouble(4000, 8000) * KM;
		planetData.minHeight = random.nextDouble(-1, -12) * KM;
		planetData.maxHeight = random.nextDouble(2, 8) * KM;
		planetData.hasOcean = true;
		planetData.craterDensity = random.nextBoolean(0.1) ? random.nextDouble() : 0.0;
		planetData.volcanoDensity = random.nextBoolean(0.1) ? random.nextDouble() : 0.0;
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

		planetData.plants = Arrays.asList(
				PlantData.of("Tundra", 0.05, 0.05, 0.3, Units.celsiusToKelvin(-5), 10, 15, 0, rgb(0.40, 0.38, 0.20)),
				PlantData.of("Grass", 0.2, 0.3, 0.5, Units.celsiusToKelvin(15), 25, 20, 0, rgb(0.29, 0.37, 0.10)),
				PlantData.of("Savanna", 0.05, 0.05, 0.5, Units.celsiusToKelvin(30), 10, 10, 0, rgb(0.55, 0.50, 0.32)),
				PlantData.of("Boreal forest", 0.04, 0.08, 0.4, Units.celsiusToKelvin(5), 10, 10, -1, rgb(0.15, 0.20, 0.03)),
				PlantData.of("Temperate forest", 0.05, 0.1, 0.8, Units.celsiusToKelvin(10), 10, 10, 0, rgb(0.16, 0.22, 0.03)),
				PlantData.of("Temperate rainforest", 1.0, 1.0, 1.0, Units.celsiusToKelvin(10), 10, 10, -1, rgb(0.27, 0.34, 0.10)),
				PlantData.of("Sub-tropical rainforest", 1.0, 1.0, 2.0, Units.celsiusToKelvin(20), 15, 15, -1, rgb(0.28, 0.40, 0.10)),
				PlantData.of("Tropical rainforest", 1.0, 1.0, 2.0, Units.celsiusToKelvin(30), 20, 20, -1, rgb(0.13, 0.23, 0.01)));
		
		NoiseVector2Value craterVerticalHeightNoiseValue = new NoiseVector2Value(
				new FractalNoise(
					0.2,
					0.00001,
					noise -> noise,
					new FractalNoise.WeightedAmplitude(),
					random),
				-1.0,
				1.0);
		NoisePolarValue craterRimRadialNoiseValue = new NoisePolarValue(
				new FractalNoise(
					0.1,
					0.0000001,
					noise -> noise,
					new FractalNoise.PersistenceAmplitude(0.4),
					random),
				0.0,
				1.0);
		NoisePolarValue craterStepsRadialNoiseValue = new NoisePolarValue(
				new FractalNoise(
					0.2,
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
						craterPart(0.0, 0.7, d -> 0.01),
						craterPart(0.5, 1.0, d -> 0.2),
						craterPart(0.5, 1.0, d -> 0.01)),
				new CraterFunction(
						craterPart(0.0, 0.4, d -> 0.0),
						craterPart(0.0, 0.7, d -> 0.01),
						craterPart(0.6, 0.7, d -> 0.0)),
				craterVerticalHeightNoiseValue,
				craterRimRadialNoiseValue,
				false,
				0.5,
				1.0);
		
		Crater simpleFlatCrater = new Crater(
				"Simple Flat Crater",
				new CraterFunction(
						craterPart(0.0, 0.6, d -> -2.2),
						craterPart(0.3, 1.0, d -> 0.8),
						craterPart(0.3, 1.0, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.7, d -> 0.01),
						craterPart(0.5, 1.0, d -> 0.1),
						craterPart(0.5, 1.0, d -> 0.01)),
				new CraterFunction(
						craterPart(0.3, 0.6, d -> 0.02),
						craterPart(0.5, 0.7, d -> 0.0)),
			craterVerticalHeightNoiseValue,
			craterRimRadialNoiseValue,
			false,
			0.5,
			1.0);
		
		Crater complexFlatCrater = new Crater(
				"Complex Flat Crater",
				new CraterFunction(
						craterPart(0.0, 0.1, d -> -0.2),
						craterPart(0.0, 0.6, d -> -1.5),
						craterPart(0.4, 1.0, d -> 0.6),
						craterPart(0.4, 1.0, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.1, d -> 0.6),
						craterPart(0.0, 0.7, d -> 0.01),
						craterPart(0.5, 1.0, d -> 0.2),
						craterPart(0.5, 1.0, d -> 0.01)),
				new CraterFunction(
						craterPart(0.4, 0.6, d -> 0.02),
						craterPart(0.5, 0.9, d -> 0.0)),
				craterVerticalHeightNoiseValue,
				craterRimRadialNoiseValue, false,
				0.5,
				1.0);
		
		Crater complexStepsCrater = new Crater(
				"Complex Steps Crater",
				new CraterFunction(
						craterPart(0.00, 0.10, d -> -0.3),
						craterPart(0.00, 0.53, d -> -0.8),
						craterPart(0.50, 0.63, d -> -0.4),
						craterPart(0.60, 0.73, d -> 0.0),
						craterPart(0.70, 1.00, d -> 0.4),
						craterPart(0.70, 1.00, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.1, d -> 0.8),
						craterPart(0.0, 0.7, d -> 0.01),
						craterPart(0.5, 1.0, d -> 0.2),
						craterPart(0.5, 1.0, d -> 0.01)),
				new CraterFunction(
						craterPart(0.0, 0.7, d -> 0.0),
						craterPart(0.4, 0.9, d -> 0.3),
						craterPart(0.4, 0.9, d -> 0.0)),
				craterVerticalHeightNoiseValue,
				craterStepsRadialNoiseValue,
				true,
				0.6,
				1.0);

		Crater complexRingsBasin = new Crater(
				"Complex Rings Basin",
				new CraterFunction(
						craterPart(0.00, 0.30, d -> -0.2),
						craterPart(0.10, 0.30, d ->  0.0),
						craterPart(0.10, 0.53, d -> -0.2),
						craterPart(0.50, 0.63, d -> -0.1),
						craterPart(0.60, 0.73, d ->  0.0),
						craterPart(0.70, 1.00, d ->  0.1),
						craterPart(0.70, 1.00, d ->  0.0)),
				new CraterFunction(
						craterPart(0.0, 0.1, d -> 0.1),
						craterPart(0.0, 0.3, d -> 0.01),
						craterPart(0.1, 0.3, d -> 0.2),
						craterPart(0.1, 0.7, d -> 0.01),
						craterPart(0.4, 1.0, d -> 0.2),
						craterPart(0.5, 1.0, d -> 0.01)),
				new CraterFunction(
						craterPart(0.0, 0.3, d -> 0.0),
						craterPart(0.1, 0.3, d -> 0.3),
						craterPart(0.1, 0.5, d -> 0.0),
						craterPart(0.4, 0.9, d -> 0.3),
						craterPart(0.4, 0.9, d -> 0.0)),
				craterVerticalHeightNoiseValue,
				craterStepsRadialNoiseValue,
				true,
				0.6,
				1.0);

		Crater domeVolcano = new Crater(
				"Dome Volcano",
				new CraterFunction(
						craterPart(0.0, 0.2, d -> 1.0),
						craterPart(0.1, 0.6, d -> 1.0),
						craterPart(0.1, 0.6, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.2, d -> 0.1),
						craterPart(0.2, 0.3, d -> 0.1),
						craterPart(0.2, 0.3, d -> 0.01)),
				new CraterFunction(
						craterPart(0.2, 0.6, d -> 0.02),
						craterPart(0.5, 1.0, d -> 0.0)),
				craterVerticalHeightNoiseValue,
				craterRimRadialNoiseValue,
				false,
				0.0,
				0.0);

		Crater stratoVolcano = new Crater(
				"Strato Volcano",
				new CraterFunction(
						craterPart(0.00, 0.02, d -> d*d*0.1 + 3.9),
						craterPart(0.00, 0.80, d -> MathUtil.smoothfloor(0.0, 1.0, (1.0 - d) * 30.0) / 30.0 * 4.0),
						craterPart(0.00, 0.80, d -> 0.0)),
				new CraterFunction(
						craterPart(0.00, 0.80, d -> 0.05),
						craterPart(0.50, 0.80, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.1, d -> 0.0),
						craterPart(0.0, 0.2, d -> 0.1),
						craterPart(0.2, 0.7, d -> 0.1),
						craterPart(0.5, 0.7, d -> 0.0)),
				craterVerticalHeightNoiseValue,
				craterRimRadialNoiseValue,
				true,
				0.0,
				0.0);

		Crater shieldVolcano = new Crater(
				"Shield Volcano",
				new CraterFunction(
						craterPart(0.00, 0.02, d -> d*d*0.1 + 1.9),
						craterPart(0.00, 0.80, d -> MathUtil.smoothfloor(0.0, 0.3, (1.0 - d) * 10.0) / 10.0 * 2.0),
						craterPart(0.00, 0.80, d -> 0.0)),
				new CraterFunction(
						craterPart(0.00, 0.04, d -> 0.1),
						craterPart(0.02, 0.80, d -> 0.01),
						craterPart(0.50, 0.80, d -> 0.0)),
				new CraterFunction(
						craterPart(0.0, 0.1, d -> 0.0),
						craterPart(0.0, 0.2, d -> 0.2),
						craterPart(0.2, 0.7, d -> 0.2),
						craterPart(0.5, 0.7, d -> 0.0)),
				craterVerticalHeightNoiseValue,
				craterRimRadialNoiseValue,
				true,
				0.0,
				0.0);

		planetData.craters = Arrays.asList(simpleRoundCrater, simpleFlatCrater, complexFlatCrater, complexStepsCrater, complexRingsBasin, domeVolcano, stratoVolcano, shieldVolcano);

		SphereValue volcanicActivityFunction = new SphereValue() {
			private final SphereValue bordersNoise = new NoiseSphereValue(
					new FractalNoise(
							Planet.RANGE_LATITUDE * 0.5,
							Planet.RANGE_LATITUDE * 0.01,
							noise -> noise,
							new FractalNoise.PersistenceAmplitude(0.4),
							random),
					-1.0,
					1.0);
			private final SphereValue activityNoise = new NoiseSphereValue(
					new FractalNoise(
							Planet.RANGE_LATITUDE * 0.4,
							Planet.RANGE_LATITUDE * 0.01,
							noise -> noise,
							new FractalNoise.PersistenceAmplitude(0.5),
							random),
					0.0,
					1.0);
			private final double borderEdge0 = random.nextDouble(0.6, 0.8);
			private final double borderEdge1 = random.nextDouble(borderEdge0, 1.0);

			private final double activityEdge0 = random.nextDouble(0.4, 0.6);
			private final double activityEdge1 = random.nextDouble(activityEdge0, 1.0);
			
			
			@Override
			public double sphereValue(double latitude, double longitude, double radius, double accuracy) {
				double border = bordersNoise.sphereValue(latitude, longitude, radius, accuracy);
				border = 1.0 - Math.abs(border);
				border = border * border;
				border = MathUtil.smoothstep(borderEdge0, borderEdge1, border);
				
				double activity = MathUtil.smoothstep(activityEdge0, activityEdge1, activityNoise.sphereValue(latitude, longitude, radius, accuracy));
				
				return border * activity;
			}
		};

		DensityFunction craterDensityFunction = new DensityFunction() {
			@Override
			public double density() {
				return planetData.craterDensity;
			}
			@Override
			public double density(double latitude, double longitude) {
				return planetData.craterDensity;
			}
		};
		DensityFunction volcanoDensityFunction = new DensityFunction() {
			@Override
			public double density() {
				return planetData.volcanoDensity;
			}

			@Override
			public double density(double latitude, double longitude) {
				return planetData.volcanoDensity * volcanicActivityFunction.sphereValue(latitude, longitude, 1.0, 0.1);
			}
		}; 
		
		double baseHeight = (planetData.maxHeight - planetData.minHeight) * 2;
		planetData.craterCalculators = Arrays.asList(
				createCraterCalculator(baseHeight,    3, random.nextLong(), craterDensityFunction, complexRingsBasin),
				createCraterCalculator(baseHeight,    5, random.nextLong(), craterDensityFunction, complexStepsCrater),
				createCraterCalculator(baseHeight,   13, random.nextLong(), craterDensityFunction, complexStepsCrater),
				createCraterCalculator(baseHeight,   11, random.nextLong(), craterDensityFunction, complexFlatCrater),
				createCraterCalculator(baseHeight,    7, random.nextLong(), craterDensityFunction, complexStepsCrater),
				createCraterCalculator(baseHeight,   79, random.nextLong(), craterDensityFunction, simpleFlatCrater),
				createCraterCalculator(baseHeight,   23, random.nextLong(), craterDensityFunction, complexFlatCrater),
				createCraterCalculator(baseHeight,  211, random.nextLong(), craterDensityFunction, simpleFlatCrater),
				createCraterCalculator(baseHeight,  919, random.nextLong(), craterDensityFunction, simpleRoundCrater),
				createCraterCalculator(baseHeight,  251, random.nextLong(), craterDensityFunction, simpleRoundCrater),
				createCraterCalculator(baseHeight, 1697, random.nextLong(), craterDensityFunction, simpleRoundCrater),
				createCraterCalculator(baseHeight, 3331, random.nextLong(), craterDensityFunction, simpleRoundCrater),
				createCraterCalculator(baseHeight, 5591, random.nextLong(), craterDensityFunction, simpleRoundCrater),
				createCraterCalculator(baseHeight, 8887, random.nextLong(), craterDensityFunction, simpleRoundCrater),

				createCraterCalculator(baseHeight,   6, random.nextLong(), volcanoDensityFunction, shieldVolcano),
				createCraterCalculator(baseHeight,   27, random.nextLong(), volcanoDensityFunction, stratoVolcano),
				createCraterCalculator(baseHeight,   31, random.nextLong(), volcanoDensityFunction, domeVolcano)
			);
		
		return planetData;
	}

    public Planet createPlanet(PlanetData planetData) {
	    return createEarth(planetData);
    }

	public Planet createEarth(PlanetData planetData) {
		// https://www.ventusky.com

        Random random = new Random(planetData.seed);

		Planet planet = new Planet();
		planet.planetData = planetData;

		planet.layers.put(LayerType.GROUND, new GroundLayer(
				RandomColor.random(random, rgb(0.70, 0.30, 0.20), rgb(0.95, 0.90, 0.70), rgb(0.50, 0.50, 0.30)),
                RandomColor.random(random, rgb(1.00, 0.99, 0.75), rgb(0.95, 0.90, 0.70), rgb(0.60, 0.50, 0.40)),
                RandomColor.random(random, rgb(1.00, 0.99, 0.75), rgb(0.95, 0.90, 0.70), rgb(0.70, 0.60, 0.50)),
                RandomColor.random(random, rgb(0.96, 0.65, 0.40), rgb(0.65, 0.45, 0.30), rgb(0.55, 0.50, 0.35)),
				RandomColor.random(random, rgb(0.73, 0.65, 0.55), rgb(0.70, 0.70, 0.60), rgb(0.60, 0.51, 0.35)),
                RandomColor.random(random, rgb(0.73, 0.65, 0.55), rgb(0.70, 0.70, 0.60), rgb(0.60, 0.51, 0.35)),
				new NoiseSphereValue(
						new FractalNoise(
							Planet.RANGE_LATITUDE * (random.nextDouble(0.2, 0.8)),
							Planet.RANGE_LATITUDE * 0.000001,
							noise -> noise,
							new FractalNoise.WeightedAmplitude(),
							random),
						planetData.minHeight,
						planetData.maxHeight),
				new NoiseVector2Value(
						new FractalNoise(
							random.nextDouble(0.001, 0.01),
							0.00001,
							noise -> noise,
							new FractalNoise.PersistenceAmplitude(random.nextDouble(0.4, 0.6)),
							random),
						0.0,
						1.0)));
		planet.layers.put(LayerType.CRATERS, new CraterLayer(
				planetData.craterCalculators));				
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
		planet.layers.put(LayerType.REEF, new ReefLayer(
				Color.WHITESMOKE,
				new NoiseSphereValue(
						new FractalNoise(
							Planet.RANGE_LATITUDE * 0.03,
							Planet.RANGE_LATITUDE * 0.001,
							noise -> noise,
							new FractalNoise.PersistenceAmplitude(0.4),
							random),
						0.0,
						1.0),
				new NoiseSphereValue(
						new FractalNoise(
							Planet.RANGE_LATITUDE * 0.01,
							Planet.RANGE_LATITUDE * 0.0000001,
							noise -> noise,
							new FractalNoise.PersistenceAmplitude(0.8),
							random),
						0.0,
						1.0)));
		planet.layers.put(LayerType.OCEAN, new OceanLayer(
				rgb(0.20, 0.14, 0.27)));
		planet.layers.put(LayerType.ATMOSPHERIC_PRESSURE, new AtmosphericPressureLayer(
				new NoiseSphereValue(
						new FractalNoise(
							Planet.RANGE_LATITUDE * 0.6,
							Planet.RANGE_LATITUDE * 0.001,
							noise -> noise,
							new FractalNoise.PersistenceAmplitude(0.5),
							random),
						-1.0,
						1.0)));
		planet.layers.put(LayerType.CLOUDS, new CloudLayer(
				new NoiseSphereValue(
						new FractalNoise(
							Planet.RANGE_LATITUDE * 0.2,
							Planet.RANGE_LATITUDE * 0.0001,
							noise -> noise,
							new FractalNoise.PersistenceAmplitude(0.5),
							random),
						0.0,
						1.0),
				new NoiseSphereValue(
						new FractalNoise(
							Planet.RANGE_LATITUDE * 0.02,
							Planet.RANGE_LATITUDE * 0.0001,
							noise -> noise,
							new FractalNoise.PersistenceAmplitude(0.5),
							random),
						0.0,
						1.0)));
		planet.layers.put(LayerType.PRECIPITATION, new PrecipitationLayer(
				-5, //K
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
				-0.5, //K
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
				RandomColor.random(random, rgb(0.66, 0.66, 0.66), Color.rgb(0.45, 0.45, 0.45)),
                RandomColor.random(random, Color.GOLD, Color.AQUAMARINE),
				new NoiseSphereValue(
						new FractalNoise(
								Planet.RANGE_LATITUDE * 0.1,
								Planet.RANGE_LATITUDE * 0.01,
								noise -> noise > 0 ? noise * noise : noise,
								new FractalNoise.WeightedAmplitude(),
								random),
						0.0,
						1.0)));
		
		return planet;
	}

	private static CraterCalculator createCraterCalculator(double baseHeight, int grid, long seed, DensityFunction densityFunction, Crater crater) {
		double heightFactor = (grid + Math.log(grid)) / 2;
		return new CraterCalculator(baseHeight / heightFactor, grid, seed, densityFunction, crater);
	}
	
	private static CraterPartFunction craterPart(double minDist, double maxDist, Function<Double, Double> func) {
		return new CraterPartFunction(minDist, maxDist, func);
	}
}
