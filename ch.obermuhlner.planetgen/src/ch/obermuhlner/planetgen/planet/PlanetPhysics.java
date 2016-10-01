package ch.obermuhlner.planetgen.planet;

public class PlanetPhysics {

	/**
	 * Returns the relative distance to the equator for a given latitude.
	 * 
	 * @param latitude the latitude
	 * @return the relative distance between 0.0 (at the equator) and 1.0 (at the poles) 
	 */
	public static double relativeDistanceToEquator(double latitude) {
		return Math.abs(latitude - Planet.EQUATOR_LATITUDE) / Planet.RANGE_LATITUDE * 2;
	}

	public static double hemisphereRelativeDistanceToEquator(double latitude) {
		return latitude - Planet.EQUATOR_LATITUDE / Planet.RANGE_LATITUDE * 2;
	}
	
	public static double latitudeToTemperatureFactor(double latitude) {
		double relativeDistance = relativeDistanceToEquator(latitude);
		if (relativeDistance < 0.2) {
			return 0;
		} else {
			return (relativeDistance - 0.2) / 0.8;
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
