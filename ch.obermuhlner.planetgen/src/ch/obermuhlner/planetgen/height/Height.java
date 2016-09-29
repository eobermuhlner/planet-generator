package ch.obermuhlner.planetgen.height;

import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public interface Height {

	double height(double latitude, double longitude, PlanetGenerationContext context);
}
