package ch.obermuhlner.planetgen.generator;

import java.util.Random;

import ch.obermuhlner.planetgen.height.FractalNoise;
import ch.obermuhlner.planetgen.height.NoiseHeight;
import ch.obermuhlner.planetgen.planet.Planet;
import javafx.scene.paint.Color;

public class PlanetGenerator {

	public Planet createPlanet(Random random) {
		Planet planet = new Planet();
		
		planet.radius = 6E6;
		planet.minHeight = -10E3;
		//planet.minHeight = -1E3;
		planet.maxHeight = 10E3;

		planet.lowVegetationColor = Color.DARKGREEN.darker().interpolate(Color.FORESTGREEN.darker(), random.nextDouble());
		planet.highVegetationColor = Color.FORESTGREEN.interpolate(Color.GREENYELLOW, random.nextDouble());
		planet.waterColor = Color.DARKBLUE.darker().interpolate(Color.BLUE, random.nextDouble());
		planet.lowGroundColor = Color.BEIGE.brighter().interpolate(Color.BROWN.darker(), random.nextDouble());
		planet.highGroundColor = Color.DARKGREY.interpolate(Color.LIGHTGREY, random.nextDouble());

		double largestFeature = random.nextDouble() * 0.5 + 0.5;
		FractalNoise.NoiseFunction noiseFunction = new FractalNoise.LinearNoise();
//		FractalNoise.NoiseFunction noiseFunction = new FractalNoise.PositiveNegativeNoise(new FractalNoise.MultipleNoise(new FractalNoise.TransformRangeNoise(0, 1, -1, 1), new FractalNoise.RidgeNoise()), new FractalNoise.LinearNoise());
		FractalNoise.AmplitudeFunction amplitudeFunction = new FractalNoise.WeightedAmplitude();
//		FractalNoise.AmplitudeFunction amplitudeFunction = new FractalNoise.PersistenceAmplitude(0.65);
		FractalNoise fractalNoise = new FractalNoise(Planet.MAX_LONGITUDE * largestFeature, noiseFunction, amplitudeFunction, random);
		planet.heightFunction = new NoiseHeight(fractalNoise, planet.minHeight, planet.maxHeight);
		
		return planet;
	}
}
