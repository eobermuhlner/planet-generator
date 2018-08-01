package ch.obermuhlner.planetgen.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetPhysics;

public class PlanetPhysicsTest {

	private static final double EPSILON = 0.00001;

	@Test
	public void testRelativeDistanceEquator() {
		assertEquals(0.0, PlanetPhysics.relativeDistanceEquator(Planet.EQUATOR_LATITUDE), EPSILON);
		assertEquals(1.0, PlanetPhysics.relativeDistanceEquator(Planet.MIN_LATITUDE), EPSILON);
		assertEquals(1.0, PlanetPhysics.relativeDistanceEquator(Planet.MAX_LATITUDE), EPSILON);
	}

	@Test
	public void testHemisphereRelativeDistanceEquator() {
		assertEquals(0.0, PlanetPhysics.hemisphereRelativeDistanceEquator(Planet.EQUATOR_LATITUDE), EPSILON);
		assertEquals(-1.0, PlanetPhysics.hemisphereRelativeDistanceEquator(Planet.MIN_LATITUDE), EPSILON);
		assertEquals(+1.0, PlanetPhysics.hemisphereRelativeDistanceEquator(Planet.MAX_LATITUDE), EPSILON);
	}
}
