package ch.obermuhlner.planetgen.test;

import ch.obermuhlner.planetgen.planet.texture.TextureWriter;
import ch.obermuhlner.planetgen.generator.PlanetGenerator;
import ch.obermuhlner.planetgen.planet.*;
import ch.obermuhlner.planetgen.planet.layer.PlanetPoint;
import ch.obermuhlner.planetgen.planet.texture.TextureType;
import ch.obermuhlner.planetgen.planet.texture.awt.BufferedImageTextureWriter;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class PlanetGeneratorTest {
    @Test
    public void testGetPlanetPoint() {
        PlanetGenerator planetGenerator = new PlanetGenerator();

        // unique seed for the planet
        long[] seed = new long[] { 123L };

        // generate planet data for unique seed
        PlanetData planetData = planetGenerator.createPlanetData(seed);

        // random generated planet data
        System.out.println("radius : " + planetData.radius + " m");
        System.out.println("revolutionTime : " + planetData.revolutionTime + " s");
        System.out.println("orbitTime : " + planetData.orbitTime + " s");

        // modify generated planet data if you need to fulfill special constraints
        planetData.baseTemperature = 290; // Celsius - expect warm tropics at the equator, small polar caps

        // create planet according to planet data constraints
        Planet planet = planetGenerator.createPlanet(planetData);

        // specify which layers and what accuracy you need (default has all layers and good enough accuracy)
        PlanetGenerationContext context = planet.createDefaultContext();

        // generate planet at one specific point (useful to know about the current location of a player in a game)
        double latitudeRadians = Math.toRadians(180) - Math.toRadians(47.2266 + 90);
        double longitudeRadians = Math.toRadians(8.8184);
        PlanetPoint planetPoint = planet.getPlanetPoint(latitudeRadians, longitudeRadians, context);
        System.out.println("height : " + planetPoint.height + " m");
        System.out.println("temperature : " + planetPoint.temperature + " K");
        System.out.println("precipitation : " + planetPoint.precipitation);
        System.out.println("color : " + planetPoint.color);
    }

    @Test
    public void testGetTextures() {
        PlanetGenerator planetGenerator = new PlanetGenerator();
        long[] seed = new long[] { 123L };
        PlanetData planetData = planetGenerator.createPlanetData(seed);
        planetData.baseTemperature = 290;

        Planet planet = planetGenerator.createPlanet(planetData);

        PlanetGenerationContext context = planet.createDefaultContext();
        context.textureTypes.addAll(Arrays.asList(TextureType.values()));

        Map<TextureType, TextureWriter<BufferedImage>> textures = planet.getTextures(512, 256, context, (width, height, textureType) -> new BufferedImageTextureWriter(width, height));

        try {
            for (TextureType textureType : TextureType.values()) {
                String filename = textureType.name().toLowerCase() + ".png";
                BufferedImage image = textures.get(textureType).getTexture();
                ImageIO.write(image, "png", new File(filename));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
