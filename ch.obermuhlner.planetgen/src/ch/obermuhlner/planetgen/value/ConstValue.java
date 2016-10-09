package ch.obermuhlner.planetgen.value;

import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public class ConstValue implements PlanetValue {

	private double value;

	public ConstValue(double value) {
		this.value = value;
	}
	
	@Override
	public double calculateValue(double latitude, double longitude, PlanetGenerationContext context) {
		return value;
	}

}
