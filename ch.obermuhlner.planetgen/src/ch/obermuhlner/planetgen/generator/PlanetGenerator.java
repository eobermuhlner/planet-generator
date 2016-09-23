package ch.obermuhlner.planetgen.generator;

import java.util.Random;

import ch.obermuhlner.planetgen.height.FractalNoise;
import ch.obermuhlner.planetgen.height.NoiseHeight;
import ch.obermuhlner.planetgen.height.PeriodicHeight;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.layer.CloudLayer;
import ch.obermuhlner.planetgen.planet.layer.GroundLayer;
import ch.obermuhlner.planetgen.planet.layer.OceanLayer;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer;
import ch.obermuhlner.planetgen.planet.layer.SnowLayer;

public class PlanetGenerator {

	private static final int KM = 1000;
	
	public Planet createPlanet(Random random) {
		return createEarth(random);
	}

	public Planet createEarth(Random random) {
		Planet planet = new Planet();
		
		PlanetData planetData = new PlanetData();
		planet.planetData = planetData;
		planetData.radius = random.nextDouble() * 4000 * KM + 4000 * KM;
		planetData.minHeight = random.nextDouble() * -10 * KM + -2 * KM;
		planetData.maxHeight = random.nextDouble() * 10 * KM + 4 * KM;
		planetData.atmosphereHeight = planetData.maxHeight * 0.8;

//		planet.lowVegetationColor = Color.DARKGREEN.darker().interpolate(Color.FORESTGREEN.darker(), random.nextDouble());
//		planet.highVegetationColor = Color.FORESTGREEN.interpolate(Color.GREENYELLOW, random.nextDouble());
//		planet.waterColor = Color.DARKBLUE.darker().interpolate(Color.BLUE, random.nextDouble());
//		planet.lowGroundColor = Color.BEIGE.brighter().interpolate(Color.BROWN.darker(), random.nextDouble());
//		planet.highGroundColor = Color.DARKGREY.interpolate(Color.LIGHTGREY, random.nextDouble());
		
		double largestFeature = random.nextDouble() * 0.5 + 0.5;
		FractalNoise.NoiseFunction noiseFunction = noise -> noise;
//		FractalNoise.NoiseFunction noiseFunction = new FractalNoise.LinearNoise();
//		FractalNoise.NoiseFunction noiseFunction = new FractalNoise.PositiveNegativeNoise(new FractalNoise.MultipleNoise(new FractalNoise.TransformRangeNoise(0, 1, -1, 1), new FractalNoise.RidgeNoise()), new FractalNoise.LinearNoise());
		FractalNoise.AmplitudeFunction amplitudeFunction = (amplitude, noise) -> {
			double signal = noise * 0.5 + 0.5;
			return amplitude * signal;
		};
//		FractalNoise.AmplitudeFunction amplitudeFunction = new FractalNoise.WeightedAmplitude();
//		FractalNoise.AmplitudeFunction amplitudeFunction = new FractalNoise.PersistenceAmplitude(0.65);
		FractalNoise groundFractalNoise = new FractalNoise(Planet.MAX_LONGITUDE * largestFeature, noiseFunction, amplitudeFunction, random);

//		FractalNoise cloudFractalNoise = new FractalNoise(
//				Planet.MAX_LONGITUDE * 0.1,
//				noise -> noise,
//				(amplitude, noise) -> amplitude * 0.5,
//				random);

		planet.layers.add(new GroundLayer(new PeriodicHeight(new NoiseHeight(groundFractalNoise, planetData.minHeight, planetData.maxHeight))));
		planet.layers.add(new OceanLayer());
		planet.layers.add(new SnowLayer());
		planet.layers.add(new PlantLayer());
		//planet.layers.add(new CloudLayer(new PeriodicHeight(new NoiseHeight(cloudFractalNoise, 0.0, 1.0))));
		
		return planet;
	}
}
