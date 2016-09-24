package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class SnowLayer implements Layer {

	private final double snowLevel = 0.8;
	
	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		double distanceToEquator = Math.abs(latitude) / Planet.MAX_LATITUDE;
		double snow;
		
		if (layerState.height <= 0) {
			double relativeHeight = Math.abs(layerState.height / planetData.minHeight);
			double temperature = Math.min(2.0, distanceToEquator + 1.0 - relativeHeight) / 2;
			snow = MathUtil.smoothstep(snowLevel, snowLevel + 0.05, temperature);
		} else {
			double relativeHeight = layerState.height / planetData.maxHeight;
			double temperature = distanceToEquator + relativeHeight / 2;
			snow = MathUtil.smoothstep(snowLevel / 2, snowLevel, temperature);
		}

		layerState.color = layerState.color.interpolate(Color.WHITE, snow);
	}
}
