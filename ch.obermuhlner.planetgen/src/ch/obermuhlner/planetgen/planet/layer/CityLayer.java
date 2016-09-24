package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class CityLayer implements Layer {

	private Height heightFunction;

	public CityLayer(Height heightFunction) {
		this.heightFunction = heightFunction;
	}
	
	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		if (layerState.height > 0) {
			double city = MathUtil.smoothstep(0.0, 1.0, heightFunction.height(latitude, longitude, accuracy));

			double distanceToEquator = Math.abs(latitude) / Planet.MAX_LATITUDE;
			double relativeHeight = layerState.height / planetData.maxHeight;
			double temperature = distanceToEquator * 0.5 + relativeHeight * 4;
			double climate = 1.0 - MathUtil.smoothstep(0.1, 0.8, temperature);
			
			city *= climate;
			
			layerState.color = layerState.color.interpolate(Color.DARKGRAY, city);
		}
	}

}
