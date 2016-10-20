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
	public double polarValue(double latitude, double longitude, PlanetGenerationContext context) {
		double value1 = valueFunction.polarValue(latitude, longitude, context);
		double value2 = valueFunction.polarValue(latitude, longitude - Planet.RANGE_LONGITUDE, context);
		double longitudeWeight = (longitude - Planet.MIN_LONGITUDE) / Planet.RANGE_LONGITUDE;
		return MathUtil.mix(value1, value2, longitudeWeight);
	}

}
