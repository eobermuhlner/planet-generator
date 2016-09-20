package ch.obermuhlner.planetgen.app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import ch.obermuhlner.planetgen.generator.PlanetGenerator;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.Planet.PlanetTextures;
import javafx.embed.swing.SwingFXUtils;

public class PlanetGeneratorApp {

	public static void main(String[] args) {
		Random random = new Random();
		
		PlanetGenerator planetGenerator = new PlanetGenerator();
		Planet planet = planetGenerator.createPlanet(random);
		
		int imageSize = 1024;
		PlanetTextures textures = planet.getTextures(imageSize, imageSize);
		
		BufferedImage swingDiffuseImage = SwingFXUtils.fromFXImage(textures.diffuseTexture, null);
		try {
			ImageIO.write(swingDiffuseImage, "PNG", new File("diffuse.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
