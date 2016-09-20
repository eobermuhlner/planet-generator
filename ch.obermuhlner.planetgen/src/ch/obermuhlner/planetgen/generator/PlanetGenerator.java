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
		planet.maxHeight = 10E3;

		planet.lowVegetationColor = Color.DARKGREEN.darker().interpolate(Color.FORESTGREEN.darker(), random.nextDouble());
		planet.highVegetationColor = Color.FORESTGREEN.interpolate(Color.GREENYELLOW, random.nextDouble());
		planet.waterColor = Color.DARKBLUE.darker().interpolate(Color.BLUE, random.nextDouble());
		planet.lowGroundColor = Color.BEIGE.brighter().interpolate(Color.BROWN.darker(), random.nextDouble());
		planet.highGroundColor = Color.DARKGREY.interpolate(Color.LIGHTGREY, random.nextDouble());

		FractalNoise fractalNoise = new FractalNoise(2000, 0.6, random);
		planet.heightFunction = new NoiseHeight(fractalNoise, planet.minHeight, planet.maxHeight);
		
		return planet;
	}
}
