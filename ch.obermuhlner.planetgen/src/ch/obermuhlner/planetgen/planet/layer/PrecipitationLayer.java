package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.NoiseHeight;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.util.Units;

public class PrecipitationLayer implements Layer {

	private final double temperatureInfluence;
	
	private NoiseHeight strongNoiseHeight;

	private NoiseHeight weakNoiseHeight;

	public PrecipitationLayer(double temperatureInfluence, NoiseHeight strongNoiseHeight, NoiseHeight weakNoiseHeight) {
		this.temperatureInfluence = temperatureInfluence;
		this.strongNoiseHeight = strongNoiseHeight;
		this.weakNoiseHeight = weakNoiseHeight;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		double precipitation = 0;
		if (planetPoint.isWater) {
			precipitation = precipitationAtLatitude(latitude) * 0.8;
		} else {
			precipitation = precipitationAtLatitude(latitude) * (1.0 - MathUtil.smoothstep(0, 10000, distanceToOcean(planetPoint, latitude, longitude, planet.planetData, context)));
		}
		
		double temperatureFactor = 1.0 - MathUtil.deviationDistance(planetPoint.temperature, Units.celsiusToKelvin(30), -50, 50);
		precipitation *= temperatureFactor;
		
		double strongNoise = strongNoiseHeight.height(latitude, longitude, context);
		precipitation *= MathUtil.smoothstep(0, 1, strongNoise);
		
		double weakNoise = weakNoiseHeight.height(latitude, longitude, context);
		precipitation *= weakNoise * 2.0 + 0.5;
		
		if (planet.planetData.hasOcean) {
			precipitation *= -planet.planetData.minHeight / (planet.planetData.maxHeight - planet.planetData.minHeight);
		} else {
			precipitation *= 0.01;
		}
		
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
		double peakNorth = (latitude - Planet.MAX_LATITUDE * 0.80) / 0.3;
		double peakSouth = (latitude - Planet.MAX_LATITUDE * 0.20) / 0.3;

		double precipitation = 0;
		precipitation += 0.9 / (1 + peakCenter*peakCenter);
		precipitation += 0.35 / (1 + peakNorth*peakNorth);
		precipitation += 0.35 / (1 + peakSouth*peakSouth);
		
		return precipitation * 2;
	}
	
	public static void main(String[] args) {
		for (double x = 0; x < Math.PI; x+=0.01) {
			double degrees = ((x / Math.PI) - 0.5) * 180;
			System.out.println(degrees + " " + precipitationAtLatitude(x));
		}
	}
}
