package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class GroundLayer implements Layer {

	public Height heightFunction;

	public GroundLayer(Height heightFunction) {
		this.heightFunction = heightFunction;
	}

	@Override
	public double getHeight(double previousHeight, PlanetData planetData, double latitude, double longitude, double accuracy) {
		return heightFunction.height(latitude, longitude, accuracy);
	}

	@Override
	public Color getColor(Color previousColor, PlanetData planetData, double height, Vector3 normals, double latitude, double longitude) {
		Color color;
		
		if (height <= 0) {
			double relativeHeight = height / planetData.minHeight;
			color = Color.BEIGE.interpolate(Color.CHOCOLATE, relativeHeight);
		} else {
			double relativeHeight = height / planetData.maxHeight;
			color = Color.BEIGE.interpolate(Color.BROWN.darker(), relativeHeight);
		}
		
		return color;
	}
}
