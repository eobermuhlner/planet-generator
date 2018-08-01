package ch.obermuhlner.planetgen.value;

import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public interface SphereValue {

	double sphereValue(double latitude, double longitude, double radius, double accuracy);

	default double sphereValue(double latitude, double longitude, PlanetGenerationContext context) {
		return sphereValue(latitude, longitude, 1.0, context.accuracy);
	}
}
