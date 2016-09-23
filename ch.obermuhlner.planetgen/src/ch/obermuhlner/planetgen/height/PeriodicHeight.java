package ch.obermuhlner.planetgen.height;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;

public class PeriodicHeight implements Height {

	private final Height heightFunction;

	public PeriodicHeight(Height heightFunction) {
		this.heightFunction = heightFunction;
	}
	
	@Override
	public double height(double latitude, double longitude, double accuracy) {
		double height1 = heightFunction.height(latitude, longitude, accuracy);
		double height2 = heightFunction.height(latitude, longitude - Planet.RANGE_LONGITUDE, accuracy);
		double longitudeWeight = (longitude - Planet.MIN_LONGITUDE) / Planet.RANGE_LONGITUDE;
		return MathUtil.mix(height1, height2, longitudeWeight);
	}

}
