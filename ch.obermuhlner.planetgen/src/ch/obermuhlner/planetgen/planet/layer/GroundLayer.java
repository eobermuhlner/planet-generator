package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class GroundLayer implements Layer {

	public Height heightFunction;

	public GroundLayer(Height heightFunction) {
		this.heightFunction = heightFunction;
	}

	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		layerState.height = heightFunction.height(latitude, longitude, accuracy);
		
		if (layerState.height <= 0) {
			double relativeHeight = layerState.height / planetData.minHeight;
			layerState.color = Color.BEIGE.interpolate(Color.CHOCOLATE, relativeHeight);
		} else {
			double relativeHeight = layerState.height / planetData.maxHeight;
			layerState.color = Color.BEIGE.interpolate(Color.BROWN.darker(), relativeHeight);
		}
	}
}
