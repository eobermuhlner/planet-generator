package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class SnowLayer implements Layer {

	private final Color snowOceanColor;
	private final Color snowGroundColor;
	private final double snowLevel = 0.8;
	
	public SnowLayer(Color snowOceanColor, Color snowGroundColor) {
		this.snowOceanColor = snowOceanColor;
		this.snowGroundColor = snowGroundColor;
	}
	
	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		double distanceToEquator = distanceToEquator(latitude);
		double snow;
		Color snowColor;
		
		if (layerState.height <= 0) {
			double relativeHeight = Math.abs(layerState.height / planetData.minHeight);
			double temperature = Math.min(2.0, distanceToEquator + 1.0 - relativeHeight) / 2;
			snow = MathUtil.smoothstep(snowLevel, snowLevel + 0.05, temperature);
			snowColor = snowOceanColor;
		} else {
			double relativeHeight = layerState.height / planetData.maxHeight;
			double temperature = distanceToEquator + relativeHeight / 2;
			snow = MathUtil.smoothstep(snowLevel / 2, snowLevel, temperature);
			snowColor = snowGroundColor;
		}

		layerState.color = layerState.color.interpolate(snowColor, snow);
	}
}
