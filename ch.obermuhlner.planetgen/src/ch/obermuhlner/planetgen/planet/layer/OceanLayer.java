package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class OceanLayer implements Layer {

	private final double transparentHeight = 5;

	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		if (layerState.height <= 0) {
			double relativeHeight = Math.min(transparentHeight, -layerState.height) / transparentHeight;
			layerState.color = layerState.color.interpolate(Color.DARKBLUE, relativeHeight);
		}
	}

}
