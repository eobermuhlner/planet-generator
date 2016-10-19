package ch.obermuhlner.planetgen.planet;

import java.util.Collections;
import java.util.List;

import ch.obermuhlner.planetgen.planet.layer.PlantLayer.PlantData;

public class PlanetData {

	public long[] seed;
	
	public double radius; // m
	public double minHeight; // m
	public double maxHeight; // m
	
	public boolean hasOcean;
	
	public double craterDensity; // 0.0 - 1.0
	
	/**
	 * Base temperature for calculations.
	 * Not the average temperature.
	 */
	public double baseTemperature; // K

	/**
	 * Base variation that depends on seasons
	 */
	public double seasonalBaseTemperatureVariation; // dK
	
	/**
	 * Base variation that depends on seasons
	 */
	public double dailyBaseTemperatureVariation; // dK
	
	/**
	 * See https://en.wikipedia.org/wiki/Lapse_rate
	 */
	public double temperatureOceanLevelToEndAtmosphere; // K

	/**
	 * Temperature difference between equator and pole.
	 * http://www-das.uwyo.edu/~geerts/cwx/notes/chap16/geo_clim.html
	 * http://earth.usc.edu/~stott/Catalina/tempdistribution.html
	 */
	public double temperatureEquatorToPole; // K
	
	public double atmosphereHeight; // m
	
	public List<PlantData> plants = Collections.emptyList();

	public double seasonTemperatureInfluenceToAverage;
	public double dailyTemperatureInfluenceToAverage;
	
	public double dailyTemperatureOceanDelay;  // 0 .. 2*PI
	public double dailyTemperatureGroundDelay;  // 0 .. 2*PI
	public double dailyTemperatureOceanFactor;

	public double season; // 0 .. 2*PI
	
	public double dayTime; // 0 .. 2*PI
}
