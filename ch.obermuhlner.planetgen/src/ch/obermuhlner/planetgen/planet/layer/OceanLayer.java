package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class OceanLayer implements Layer {

	private final Color oceanColor;
	private final double transparentHeight = 5;

	public OceanLayer(Color oceanColor) {
		this.oceanColor = oceanColor;
	}
	
	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		if (layerState.height <= 0) {
			double relativeHeight = Math.min(transparentHeight, -layerState.height) / transparentHeight;
			layerState.color = layerState.color.interpolate(oceanColor, relativeHeight);
		}
	}

}
