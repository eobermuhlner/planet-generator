package ch.obermuhlner.planetgen.generator;

import java.util.Random;

import ch.obermuhlner.planetgen.height.FractalNoise;
import ch.obermuhlner.planetgen.height.NoiseHeight;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.layer.CityLayer;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer;
import ch.obermuhlner.planetgen.planet.layer.GroundLayer;
import ch.obermuhlner.planetgen.planet.layer.IceLayer;
import ch.obermuhlner.planetgen.planet.layer.OceanLayer;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer;
import ch.obermuhlner.planetgen.planet.layer.SnowLayer;
import javafx.scene.paint.Color;

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

		double largestFeature = random.nextDouble() * 0.6 + 0.2;
		FractalNoise.NoiseFunction noiseFunction = noise -> noise;
		FractalNoise.AmplitudeFunction amplitudeFunction = (amplitude, noise) -> {
			double signal = noise * 0.5 + 0.5;
			return amplitude * signal;
		};
		FractalNoise groundFractalNoise = new FractalNoise(
				Planet.RANGE_LATITUDE * largestFeature,
				Planet.RANGE_LATITUDE * 0.001,
				noiseFunction,
				amplitudeFunction,
				random);

		FractalNoise cityFractalNoise = new FractalNoise(
				Planet.RANGE_LATITUDE * 0.1,
				Planet.RANGE_LATITUDE * 0.01,
				noise -> noise > 0 ? noise * noise : noise,
				new FractalNoise.WeightedAmplitude(),
				random);

//		FractalNoise cloudFractalNoise = new FractalNoise(
//				Planet.MAX_LONGITUDE * 0.2,
//				Planet.MAX_LONGITUDE * 0.01,
//				noise -> noise,
//				(amplitude, noise) -> amplitude * 0.5,
//				random);

		planet.layers.add(new GroundLayer(
				Color.CHOCOLATE.interpolate(Color.BEIGE.darker(), random.nextDouble()),
				Color.BEIGE.brighter().interpolate(Color.CORAL.darker(), random.nextDouble()),
				Color.BEIGE.brighter().interpolate(Color.BROWN.darker(), random.nextDouble()),
				Color.DARKGREY.interpolate(Color.LIGHTGREY, random.nextDouble()),
				new NoiseHeight(groundFractalNoise, planetData.minHeight, planetData.maxHeight)));
//		planet.layers.add(new CraterLayer());
		planet.layers.add(new OceanLayer(
				Color.DARKBLUE.darker().interpolate(Color.BLUE, random.nextDouble())));
		planet.layers.add(new IceLayer(
				Color.WHITE));
		planet.layers.add(new PlantLayer(
				Color.DARKGREEN.darker().interpolate(Color.FORESTGREEN.darker(), random.nextDouble()), 
				Color.FORESTGREEN.brighter().interpolate(Color.GREENYELLOW, random.nextDouble())));
		planet.layers.add(new SnowLayer(
				Color.WHITE));
		planet.layers.add(new CityLayer(
				Color.DARKGRAY.interpolate(Color.GRAY, random.nextDouble()),
				Color.GOLD.interpolate(Color.AQUAMARINE, random.nextDouble()),
				new NoiseHeight(cityFractalNoise, 0.0, 1.0)));
//		planet.layers.add(new CloudLayer(Color.WHITE, new PeriodicHeight(new NoiseHeight(cloudFractalNoise, 0.0, 1.0))));
		
		return planet;
	}
}
