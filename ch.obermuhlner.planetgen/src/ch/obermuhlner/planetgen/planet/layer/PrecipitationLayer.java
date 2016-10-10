package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.NoiseValue;
import ch.obermuhlner.util.Units;

public class PrecipitationLayer implements Layer {

	private final double temperatureAverageInfluence;
	
	private NoiseValue averageGlobalNoise;

	private NoiseValue averageLocalNoise;

	private double temperatureInfluence;

	private NoiseValue globalNoise;

	private NoiseValue localNoise;

	public PrecipitationLayer(double temperatureAverageInfluence, NoiseValue averageGlobalNoise, NoiseValue averageLocalNoise, double temperatureInfluence, NoiseValue currentGlobalNoise, NoiseValue currentLocalNoise) {
		this.temperatureAverageInfluence = temperatureAverageInfluence;
		this.averageGlobalNoise = averageGlobalNoise;
		this.averageLocalNoise = averageLocalNoise;
		this.temperatureInfluence = temperatureInfluence;
		this.globalNoise = currentGlobalNoise;
		this.localNoise = currentLocalNoise;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		double precipitationAverage = 0;
		precipitationAverage = precipitationAtLatitude(latitude) * precipitationAtHeight(planetPoint);
		
		double temperatureFactor = 1.0 - MathUtil.deviationDistance(planetPoint.temperature, Units.celsiusToKelvin(30), -50, 50);
		precipitationAverage *= temperatureFactor;
		precipitationAverage *= MathUtil.smoothstep(0.2, 0.8, averageGlobalNoise.calculateValue(latitude, longitude, context));
		precipitationAverage *= averageLocalNoise.calculateValue(latitude, longitude, context) * 2.0 + 0.5;
		
		if (planet.planetData.hasOcean) {
			precipitationAverage *= -planet.planetData.minHeight / (planet.planetData.maxHeight - planet.planetData.minHeight);
		} else {
			precipitationAverage *= 0.01;
		}
		
		double precipitation = precipitationAverage;
		precipitation *= ridge(MathUtil.smoothstep(temperatureFactor * 0.7, temperatureFactor, globalNoise.calculateValue(latitude, longitude, context)) - 0.5) * 4.0;
		precipitation *= MathUtil.smoothstep(0.5, 1.0, localNoise.calculateValue(latitude, longitude, context)); 
		
		planetPoint.precipitationAverage = precipitationAverage;
		planetPoint.precipitation = precipitation;
		
		planetPoint.temperatureAverage += planetPoint.precipitationAverage * temperatureAverageInfluence;
		planetPoint.temperature += planetPoint.precipitation * temperatureInfluence;
	}
	
	private double ridge(double value) {
		//return value * value * value;
		return Math.exp(-8 * value * value);
	}

	private double precipitationAtHeight(PlanetPoint planetPoint) {
		double height = planetPoint.groundHeight;
		if (planetPoint.isWater) {
			height = 0;
		}
		return 1.0 - MathUtil.deviationDistance(height, 2000, -20000, 10000);
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
