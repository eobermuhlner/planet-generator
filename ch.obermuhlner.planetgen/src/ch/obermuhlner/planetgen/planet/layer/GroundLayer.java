package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class GroundLayer implements Layer {

	private Color deepOceanFloorColor;
	private Color shallowOceanFloorColor;
	private Color lowGroundColor;
	private Color highCroundColor;
	
	private Height heightFunction;

	public GroundLayer(Color deepOceanFloorColor, Color shallowOceanFloorColor, Color lowGroundColor, Color highCroundColor, Height heightFunction) {
		this.deepOceanFloorColor = deepOceanFloorColor;
		this.shallowOceanFloorColor = shallowOceanFloorColor;
		this.lowGroundColor = lowGroundColor;
		this.highCroundColor = highCroundColor;
		this.heightFunction = heightFunction;
	}

	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		layerState.height += heightFunction.height(latitude, longitude, accuracy);
		
		if (layerState.height <= 0) {
			double relativeHeight = layerState.height / planetData.minHeight;
			layerState.color = shallowOceanFloorColor.interpolate(deepOceanFloorColor, relativeHeight);
		} else {
			double relativeHeight = layerState.height / planetData.maxHeight;
			layerState.color = lowGroundColor.interpolate(highCroundColor.darker(), relativeHeight);
		}
	}
}
