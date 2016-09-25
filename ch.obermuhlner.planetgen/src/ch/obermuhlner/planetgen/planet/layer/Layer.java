package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;

public interface Layer {

	void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy);

	default double distanceToEquator(double latitude) {
		return Math.abs(latitude - (Planet.MAX_LATITUDE + Planet.MIN_LATITUDE) / 2) / Planet.RANGE_LATITUDE * 2;
	}
}
