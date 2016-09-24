package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class PlantLayer implements Layer {

	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		if (layerState.height > 0) {
			double distanceToEquator = Math.abs(latitude) / Planet.MAX_LATITUDE;
			double relativeHeight = layerState.height / planetData.maxHeight;
			double temperature = Math.min(2.0, distanceToEquator + relativeHeight * 2) / 2;
			Color plantColor = Color.DARKGREEN.darker().interpolate(Color.GREENYELLOW, temperature);
			double vegetation = 1.0 - MathUtil.smoothstep(0.1, 0.8, temperature);
			layerState.color = layerState.color.interpolate(plantColor, vegetation);
		}
	}
}