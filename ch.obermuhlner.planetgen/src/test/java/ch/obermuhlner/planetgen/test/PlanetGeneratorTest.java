package ch.obermuhlner.planetgen.test;

import ch.obermuhlner.planetgen.planet.texture.awt.BufferedImagePlanetTextures;
import ch.obermuhlner.planetgen.generator.PlanetGenerator;
import ch.obermuhlner.planetgen.planet.*;
import ch.obermuhlner.planetgen.planet.layer.PlanetPoint;
import ch.obermuhlner.planetgen.planet.texture.TextureType;
import ch.obermuhlner.util.Random;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class PlanetGeneratorTest {
    @Test
    public void testGetPlanetPoint() {
        PlanetGenerator planetGenerator = new PlanetGenerator();
        PlanetData planetData = planetGenerator.createPlanetData(123L);
        //planetData.baseTemperature = 240;

        Planet planet = planetGenerator.createPlanet(planetData, new Random(planetData.seed));

        PlanetGenerationContext context = planet.createDefaultContext();

        double latitudeRadians = Math.toRadians(180) - Math.toRadians(47.2266 + 90);
        double longitudeRadians = Math.toRadians(8.8184);
        PlanetPoint planetPoint = planet.getPlanetPoint(latitudeRadians, longitudeRadians, context);
        System.out.println("groundHeight : " + planetPoint.groundHeight);
        System.out.println("iceHeight : " + planetPoint.iceHeight);
        System.out.println("snowHeight : " + planetPoint.snowHeight);
        System.out.println("height : " + planetPoint.height);
        System.out.println("temperature : " + planetPoint.temperature);
        System.out.println("color : " + planetPoint.color);
    }

    @Test
    public void testGetTextures() {
        PlanetGenerator planetGenerator = new PlanetGenerator();
        PlanetData planetData = planetGenerator.createPlanetData(123L);

        Planet planet = planetGenerator.createPlanet(planetData, new Random(planetData.seed));

        PlanetGenerationContext context = planet.createDefaultContext();
        context.textureTypes.addAll(Arrays.asList(TextureType.values()));

        BufferedImagePlanetTextures planetTextures = new BufferedImagePlanetTextures(1024, 512);
        planet.getTextures(1024, 512, context, planetTextures);

        try {
            for (TextureType textureType : TextureType.values()) {
                String filename = textureType.name().toLowerCase() + ".png";
                ImageIO.write(planetTextures.getImage(textureType), "png", new File(filename));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
