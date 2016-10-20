package ch.obermuhlner.planetgen.value;

import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public class ConstSphereValue implements SphereValue {

	private double value;

	public ConstSphereValue(double value) {
		this.value = value;
	}
	
	@Override
	public double polarValue(double latitude, double longitude, PlanetGenerationContext context) {
		return value;
	}

}
