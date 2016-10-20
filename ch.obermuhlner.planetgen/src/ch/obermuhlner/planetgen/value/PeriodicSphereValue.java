package ch.obermuhlner.planetgen.value;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public class PeriodicSphereValue implements SphereValue {

	private final SphereValue valueFunction;

	public PeriodicSphereValue(SphereValue valueFunction) {
		this.valueFunction = valueFunction;
	}
	
	@Override
	public double sphereValue(double latitude, double longitude, PlanetGenerationContext context) {
		double value1 = valueFunction.sphereValue(latitude, longitude, context);
		double value2 = valueFunction.sphereValue(latitude, longitude - Planet.RANGE_LONGITUDE, context);
		double longitudeWeight = (longitude - Planet.MIN_LONGITUDE) / Planet.RANGE_LONGITUDE;
		return MathUtil.mix(value1, value2, longitudeWeight);
	}

}
