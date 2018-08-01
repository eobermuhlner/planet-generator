package ch.obermuhlner.planetgen.planet;

public class PlanetPhysics {

	/**
	 * Returns the relative distance to the equator for a given latitude.
	 * 
	 * @param latitude the latitude
	 * @return the relative distance to the equator between 0.0 (at the equator) and 1.0 (at the poles) 
	 */
	public static double relativeDistanceEquator(double latitude) {
		return Math.abs(latitude - Planet.EQUATOR_LATITUDE) / Planet.RANGE_LATITUDE * 2;
	}

	/**
	 * Returns the relative distance to the equator for a given latitude preserving the sign of the hemisphere.
	 * 
	 * @param latitude the latitude
	 * @return the relative distance to the equator between -1.0 (at the south pole), 0.0 (at the equator) and 1.0 (at the north pole) 
	 */
	public static double hemisphereRelativeDistanceEquator(double latitude) {
		return (latitude - Planet.EQUATOR_LATITUDE) / Planet.RANGE_LATITUDE * 2;
	}
	
	public static double distanceEquatorToTemperatureFactor(double relativeDistanceToEquator) {
		if (Math.abs(relativeDistanceToEquator) < 0.2) {
			return 0;
		} else {
			return Math.signum(relativeDistanceToEquator) * (Math.abs(relativeDistanceToEquator) - 0.2) / 0.8;
		}
	}
	
	public static double heightToTemperatureFactor(double height) {
		final double maxHeight = 11000;
		
		if (height > maxHeight) {
			return 1.0;
		}
		
		return height / maxHeight;
	}

}
