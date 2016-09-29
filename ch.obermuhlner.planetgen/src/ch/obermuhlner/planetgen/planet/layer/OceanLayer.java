package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import javafx.scene.paint.Color;

public class OceanLayer implements Layer {

	private final Color oceanColor;
	private final double transparentHeight = 5;

	public OceanLayer(Color oceanColor) {
		this.oceanColor = oceanColor;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context) {
		planetPoint.oceanColor = oceanColor;
		
		if (planetPoint.height <= 0) {
			double relativeHeight = Math.min(transparentHeight, -planetPoint.height) / transparentHeight;
			planetPoint.color = planetPoint.color.interpolate(oceanColor, relativeHeight);
			
			planetPoint.height = 0;
		}
	}

}
