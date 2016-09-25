package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;

public interface Layer {

	void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy);

	/**
	 * Returns the relative distance to the equator for a given latitude.
	 * 
	 * @param latitude the latitude
	 * @return the relative distance between 0.0 (at the equator) and 1.0 (at the poles) 
	 */
	default double relativeDistanceToEquator(double latitude) {
		return Math.abs(latitude - Planet.EQUATOR_LATITUDE) / Planet.RANGE_LATITUDE;
	}
}
