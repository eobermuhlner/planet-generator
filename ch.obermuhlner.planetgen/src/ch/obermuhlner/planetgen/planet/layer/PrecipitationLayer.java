package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.NoiseHeight;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public class PrecipitationLayer implements Layer {

	private final double temperatureInfluence;
	
	private NoiseHeight noiseHeight;

	public PrecipitationLayer(double temperatureInfluence, NoiseHeight noiseHeight) {
		this.temperatureInfluence = temperatureInfluence;
		this.noiseHeight = noiseHeight;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context) {
		double precipitation = 0;
		if (planetPoint.isWater) {
			precipitation = precipitationAtLatitude(latitude);
		} else {
			precipitation = precipitationAtLatitude(latitude) * (1.0 - MathUtil.smoothstep(0, 10000, distanceToOcean(planetPoint, latitude, longitude, planetData, context)));
		}
		double noise = 0.8 + 0.4 * noiseHeight.height(latitude, longitude, context);
		precipitation *= noise;
		
		planetPoint.precipitationAverage = precipitation;
		planetPoint.precipitation = precipitation;
		
		planetPoint.temperatureAverage += planetPoint.precipitationAverage * temperatureInfluence;
		planetPoint.temperature += planetPoint.precipitation * temperatureInfluence;
	}
	
	private double distanceToOcean(PlanetPoint planetPoint, double latitude, double longitude, PlanetData planetData, PlanetGenerationContext context) {
		if (planetPoint.isWater) {
			return 0;
		}
		return planetPoint.height;
	}

	/*
	 * http://www.roperld.com/science/Evaporation_PrecipitationVSLatitude.htm
	 */
	private static double precipitationAtLatitude(double latitude) {
		double peakCenter = (latitude - Planet.MAX_LATITUDE * 0.50) / 0.08;
		double peakNorth = (latitude - Planet.MAX_LATITUDE * 0.70) / 0.3;
		double peakSouth = (latitude - Planet.MAX_LATITUDE * 0.30) / 0.3;

		double precipitation = 0;
		precipitation += 0.9 / (1 + peakCenter*peakCenter);
		precipitation += 0.2 / (1 + peakNorth*peakNorth);
		precipitation += 0.2 / (1 + peakSouth*peakSouth);
		
		return precipitation;
	}
	
	public static void main(String[] args) {
		for (double x = 0; x < Math.PI; x+=0.01) {
			double degrees = ((x / Math.PI) - 0.5) * 180;
			System.out.println(degrees + " " + precipitationAtLatitude(x));
		}
	}
}
