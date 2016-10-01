package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.NoiseHeight;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.PlanetPhysics;
import ch.obermuhlner.planetgen.math.Color;

public class PlantLayer implements Layer {

	private final Color lowGroundPlantColor;
	private final Color highGroundPlantColor;
	private NoiseHeight noiseHeight;

	public PlantLayer(Color lowGroundPlantColor, Color highGroundPlantColor, NoiseHeight noiseHeight) {
		this.lowGroundPlantColor = lowGroundPlantColor;
		this.highGroundPlantColor = highGroundPlantColor;
		this.noiseHeight = noiseHeight;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context) {
		if (planetPoint.height > 0 && planetPoint.iceHeight == 0) {
			double distanceToEquator = PlanetPhysics.relativeDistanceToEquator(latitude);
			double relativeHeight = planetPoint.height / planetData.maxHeight;
			double temperature = Math.min(2.0, distanceToEquator + relativeHeight * 2) / 2;
			double noise = noiseHeight.height(latitude, longitude, context);
			planetPoint.plantColor = lowGroundPlantColor.interpolate(highGroundPlantColor, temperature);
			double vegetation = 1.0 - MathUtil.smoothstep(0.1, 0.8, temperature);
			vegetation *= 0.8 + MathUtil.smoothstep(0.0, 1.0, noise) * 0.2;
			planetPoint.color = planetPoint.color.interpolate(planetPoint.plantColor, vegetation);
		} else {
			planetPoint.plantColor = null;
		}
	}
}
