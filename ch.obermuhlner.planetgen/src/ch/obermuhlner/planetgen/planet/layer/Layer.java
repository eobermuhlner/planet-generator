package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public interface Layer {

	double getHeight(double previousHeight, PlanetData planetData, double latitude, double longitude, double accuracy);
	
	Color getColor(Color previousColor, PlanetData planetData, double height, Vector3 normals, double latitude, double longitude);
}
