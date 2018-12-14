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
    private Planet generatePlanet() {
        PlanetGenerator planetGenerator = new PlanetGenerator();

        // unique seed for the planet
        long[] seed = new long[] { 4 };

        // generate planet data for unique seed
        PlanetData planetData = planetGenerator.createPlanetData(seed);

        // print some values of generated planet
        System.out.println("radius : " + planetData.radius + " m");
        System.out.println("revolutionTime : " + planetData.revolutionTime + " s");
        System.out.println("orbitTime : " + planetData.orbitTime + " s");

        // modify generated planet data if you need to fulfill special constraints
        planetData.baseTemperature = 290; // Kelvin - expect warm tropics at the equator, small polar caps

        // create planet according to planet data constraints
        Planet planet = planetGenerator.createPlanet(planetData);

        return planet;
    }

    @Test
    public void testGetPlanetPoint() {
        Planet planet = generatePlanet();

        // specify which layers and what accuracy you need (default has all layers and good enough accuracy)
        PlanetGenerationContext context = planet.createDefaultContext();

        // generate planet at one specific point (useful to know about the current location of a player in a game)
        double latitudeRadians = Math.toRadians(90.0);
        double longitudeRadians = Math.toRadians(0.0);
        PlanetPoint planetPoint = planet.getPlanetPoint(latitudeRadians, longitudeRadians, context);

        // print some values for the specific point on the planet
        System.out.println("height : " + planetPoint.height + " m");
        System.out.println("temperature : " + planetPoint.temperature + " K");
        System.out.println("precipitation : " + planetPoint.precipitation);
        System.out.println("color : " + planetPoint.color);
    }

    @Test
    public void testGetTextures() {
        Planet planet = generatePlanet();

        // specify context and add the texture types you want to generate (we simply add all of them)
        PlanetGenerationContext context = planet.createDefaultContext();
        context.textureTypes.addAll(Arrays.asList(TextureType.values()));

        // generate 512 x 256 pixel textures for the entire planet
        Map<TextureType, TextureWriter<BufferedImage>> textures = planet.getTextures(512, 256, context, (width, height, textureType) -> new BufferedImageTextureWriter(width, height));

        // save the textures into png files
        try {
            for (Map.Entry<TextureType, TextureWriter<BufferedImage>> entry : textures.entrySet()) {
                String filename = entry.getKey().name().toLowerCase() + ".png";
                BufferedImage image = entry.getValue().getTexture();
                ImageIO.write(image, "png", new File(filename));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetTerrainTextures() {
        Planet planet = generatePlanet();

        // specify context and add the texture types you want to generate (we simply add all of them)
        PlanetGenerationContext context = planet.createDefaultContext();
        context.accuracy = 1; // meters
        context.textureTypes.addAll(Arrays.asList(TextureType.values()));

        // create terrainHeightMap to be filled
        DoubleMap terrainHeightMap = new DoubleMap(16, 16);

        // generate terrain textures and fill terrain height map for the specified area
        Map<TextureType, TextureWriter<BufferedImage>> textures = planet.getTextures(
                Math.toRadians(90.0), Math.toRadians(100.0), Math.toRadians(180.0), Math.toRadians(190.0),
                64, 64, context, (width, height, textureType) -> new BufferedImageTextureWriter(width, height), terrainHeightMap);

        // save the textures into png files
        try {
            for (Map.Entry<TextureType, TextureWriter<BufferedImage>> entry : textures.entrySet()) {
                String filename = "terrain_" + entry.getKey().name().toLowerCase() + ".png";
                BufferedImage image = entry.getValue().getTexture();
                ImageIO.write(image, "png", new File(filename));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // use the terrain height map to create the 3D mesh
        for(int y = 0; y < terrainHeightMap.height; y++) {
            for (int x = 0; x < terrainHeightMap.width; x++) {
                if (x > 0) {
                    System.out.print(", ");
                }
                System.out.print(terrainHeightMap.getValue(x, y));
            }
            System.out.println();
        }
    }
}
