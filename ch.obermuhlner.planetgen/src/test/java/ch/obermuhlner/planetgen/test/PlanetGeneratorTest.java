package ch.obermuhlner.planetgen.test;

import ch.obermuhlner.planetgen.generator.PlanetGenerator;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.layer.PlanetPoint;
import ch.obermuhlner.util.Random;
import org.junit.Test;

import java.util.Objects;

public class PlanetGeneratorTest {
    @Test
    public void testPlanetGenerator() {
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
}
