package ch.obermuhlner.planetgen.generator;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.layer.Layer;
import javafx.scene.paint.Color;

public class PlantLayer implements Layer {

	@Override
	public double getHeight(double previousHeight, PlanetData planetData, double latitude, double longitude, double accuracy) {
		return previousHeight;
	}

	@Override
	public Color getColor(Color previousColor, PlanetData planetData, double height, Vector3 normals, double latitude, double longitude) {
		double distanceToEquator = Math.abs(latitude) / Planet.MAX_LATITUDE;
		Color color;
		
		if (height <= 0) {
			color = previousColor;
		} else {
			double relativeHeight = height / planetData.maxHeight;
			double temperature = Math.min(2.0, distanceToEquator + relativeHeight * 2) / 2;
			Color plantColor = Color.DARKGREEN.darker().interpolate(Color.GREENYELLOW, temperature);
			double vegetation = 1.0 - MathUtil.smoothstep(0.1, 0.8, temperature);
			color = previousColor.interpolate(plantColor, vegetation);
		}
		
		return color;
	}
}
