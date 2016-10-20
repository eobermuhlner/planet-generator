package ch.obermuhlner.planetgen.value;

import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public interface PolarValue {

	double polarValue(double angle, double radius, PlanetGenerationContext context);
}
