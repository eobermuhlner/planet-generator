package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.PlanetData;

public interface Layer {

	void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy);
	
}
