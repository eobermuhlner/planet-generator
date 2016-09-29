package ch.obermuhlner.planetgen.height;

import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public class ConstHeight implements Height {

	private double height;

	public ConstHeight(double height) {
		this.height = height;
	}
	
	@Override
	public double height(double latitude, double longitude, PlanetGenerationContext context) {
		return height;
	}

}
