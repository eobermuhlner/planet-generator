package ch.obermuhlner.planetgen.planet;

import java.util.Collections;
import java.util.List;

import ch.obermuhlner.planetgen.planet.layer.CraterLayer.Crater;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.CraterCalculator;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer.PlantData;

public class PlanetData {

	public long[] seed;
	
	public long time; // ms
	public long orbitTime; // ms
	public long orbitTimeOffset; // ms
	public long revolutionTime; // ms
	public long revolutionTimeOffset; // ms
	
	public double radius; // m
	public double minHeight; // m
	public double maxHeight; // m
	
	public boolean hasOcean;
	
	public double craterDensity; // 0.0 - 1.0
	public double volcanoDensity; // 0.0 - 1.0
	
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
	public List<Crater> craters = Collections.emptyList();
	public List<CraterCalculator> craterCalculators = Collections.emptyList();

	public double seasonTemperatureInfluenceToAverage;
	public double dailyTemperatureInfluenceToAverage;
	
	public double dailyTemperatureOceanDelay;  // 0 .. 2*PI
	public double dailyTemperatureGroundDelay;  // 0 .. 2*PI
	public double dailyTemperatureOceanFactor;

	public double getRevolution() {
		long revolution = (time + revolutionTimeOffset) % revolutionTime;
		return ((double) revolution) / revolutionTime;
	}
	
	public double getOrbit() {
		long orbit = (time + orbitTimeOffset) % orbitTime;
		return ((double) orbit) / revolutionTime;
	}
	
	public double getOceanPart() {
		if (hasOcean && minHeight < 0) {
			if (maxHeight < 0) {
				return 1.0;
			}
			
			return -minHeight / (maxHeight - minHeight);
		}
		
		return 0.0;
	}
}
