package ch.obermuhlner.planetgen.value;

import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public interface PlanetValue {

	double calculateValue(double latitude, double longitude, PlanetGenerationContext context);
}
