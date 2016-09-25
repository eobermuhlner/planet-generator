package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class IceLayer implements Layer {

	private final double oceanIceLevel = 0.8;
	private final double groundIceLevel = 0.8;
	private final Color iceColor;

	public IceLayer(Color iceColor) {
		this.iceColor = iceColor;
	}
	
	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		double distanceToEquator = relativeDistanceToEquator(latitude);
		if (layerState.height <= 0) {
			double relativeHeight = Math.abs(layerState.height / planetData.minHeight);
			double temperature = Math.min(2.0, distanceToEquator + 1.0 - relativeHeight) / 2;
			double ice = MathUtil.smoothstep(oceanIceLevel, oceanIceLevel + 0.05, temperature);
			double iceHeight = ice * 100; 
			
			layerState.height += iceHeight;
			layerState.color = layerState.color.interpolate(iceColor, ice);			
		} else {
			double relativeHeight = layerState.height / planetData.maxHeight;
			double temperature = distanceToEquator + relativeHeight / 2;
			double ice = MathUtil.smoothstep(groundIceLevel, groundIceLevel + 0.1, temperature);
			double iceHeight = ice * 2000; 
			
			layerState.height += iceHeight;
			layerState.color = layerState.color.interpolate(iceColor, ice);
		}
	}

}
