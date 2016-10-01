package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public interface Layer {

	void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context);
}
