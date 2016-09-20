package ch.obermuhlner.planetgen.generator;

import java.util.Random;

import ch.obermuhlner.planetgen.height.FractalNoise;
import ch.obermuhlner.planetgen.height.NoiseHeight;
import ch.obermuhlner.planetgen.planet.Planet;

public class PlanetGenerator {

	public Planet createPlanet(Random random) {
		Planet planet = new Planet();
		
		planet.radius = 6E6;
		planet.minHeight = -10E3;
		planet.maxHeight = 10E3;
		
		FractalNoise fractalNoise = new FractalNoise(2000, 0.6, random);
		planet.heightFunction = new NoiseHeight(fractalNoise, planet.minHeight, planet.maxHeight);
		
		return planet;
	}
}
