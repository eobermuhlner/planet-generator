package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class OceanLayer implements Layer {

	private final double transparentHeight = 5;

	@Override
	public double getHeight(double previousHeight, PlanetData planetData, double latitude, double longitude, double accuracy) {
		return previousHeight;
	}

	@Override
	public Color getColor(Color previousColor, PlanetData planetData, double height, Vector3 normals, double latitude, double longitude) {
		Color color;
		
		if (height <= 0) {
			double relativeHeight = Math.min(transparentHeight, -height) / transparentHeight;
			color = previousColor.interpolate(Color.DARKBLUE, relativeHeight);
		} else {
			color = previousColor;
		}
		
		return color;
	}

}
