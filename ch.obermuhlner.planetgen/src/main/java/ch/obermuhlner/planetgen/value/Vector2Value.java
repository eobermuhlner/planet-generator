package ch.obermuhlner.planetgen.value;

import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public interface Vector2Value {

	double vector2Value(Vector2 value, double accuracy);

	default double vector2Value(Vector2 value, PlanetGenerationContext context) {
		return vector2Value(value, context.accuracy);
	}
}
