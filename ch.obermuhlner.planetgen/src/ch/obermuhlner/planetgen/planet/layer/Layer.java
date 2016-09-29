package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public interface Layer {

	void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context);

	/**
	 * Returns the relative distance to the equator for a given latitude.
	 * 
	 * @param latitude the latitude
	 * @return the relative distance between 0.0 (at the equator) and 1.0 (at the poles) 
	 */
	default double relativeDistanceToEquator(double latitude) {
		return Math.abs(latitude - Planet.EQUATOR_LATITUDE) / Planet.RANGE_LATITUDE * 2;
	}

	default double hemisphereRelativeDistanceToEquator(double latitude) {
		return latitude - Planet.EQUATOR_LATITUDE / Planet.RANGE_LATITUDE * 2;
	}
}
