package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public class OceanLayer implements Layer {

	private static final Color OCEAN_SPECULAR_COLOR = Color.rgb(0.5, 0.5, 0.5);

	private final Color oceanColor;
	private final double transparentHeight = 50;

	public OceanLayer(Color oceanColor) {
		this.oceanColor = oceanColor;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		if (planet.planetData.hasOcean) {
			planetPoint.oceanColor = oceanColor;
			
			if (planetPoint.height <= 0) {
				double relativeHeight = Math.min(transparentHeight, -planetPoint.height) / transparentHeight;

				planetPoint.color = planetPoint.color.interpolate(oceanColor, relativeHeight);
				planetPoint.specularColor = planetPoint.specularColor.interpolate(OCEAN_SPECULAR_COLOR, relativeHeight);
				planetPoint.height = 0;
			}
		}
	}

}
