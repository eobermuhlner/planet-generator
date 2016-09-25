package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class PlantLayer implements Layer {

	private final Color lowGroundPlantColor;
	private final Color highGroundPlantColor;

	public PlantLayer(Color lowGroundPlantColor, Color highGroundPlantColor) {
		this.lowGroundPlantColor = lowGroundPlantColor;
		this.highGroundPlantColor = highGroundPlantColor;
	}
	
	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		if (layerState.height > 0) {
			double distanceToEquator = relativeDistanceToEquator(latitude);
			double relativeHeight = layerState.height / planetData.maxHeight;
			double temperature = Math.min(2.0, distanceToEquator + relativeHeight * 2) / 2;
			Color plantColor = lowGroundPlantColor.darker().interpolate(highGroundPlantColor, temperature);
			double vegetation = 1.0 - MathUtil.smoothstep(0.1, 0.8, temperature);
			layerState.color = layerState.color.interpolate(plantColor, vegetation);
		}
	}
}
