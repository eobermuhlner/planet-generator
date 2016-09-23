package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class CloudLayer implements Layer {

	public Height heightFunction;

	public CloudLayer(Height heightFunction) {
		this.heightFunction = heightFunction;
	}

	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		layerState.height = Math.max(layerState.height, planetData.atmosphereHeight);
		
		double relativeHeight = layerState.height / planetData.atmosphereHeight;
		layerState.color = layerState.height > planetData.atmosphereHeight ? layerState.color : layerState.color.interpolate(Color.MAGENTA, relativeHeight); 
	}
	
}
