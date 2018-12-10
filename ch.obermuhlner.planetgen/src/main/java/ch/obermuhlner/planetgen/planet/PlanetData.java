package ch.obermuhlner.planetgen.planet;

import java.util.Collections;
import java.util.List;

import ch.obermuhlner.planetgen.planet.layer.CraterLayer.Crater;
import ch.obermuhlner.planetgen.planet.layer.CraterLayer.CraterCalculator;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer.PlantData;

/**
 * Data of a planet to be generated.
 *
 * An instance of {@link PlanetData} is normally created using
 * {@link ch.obermuhlner.planetgen.generator.PlanetGenerator#createPlanetData(long...)}.
 */
public class PlanetData {

	public long[] seed;
	
	public long time; // ms
	public long orbitTime; // ms
	public long orbitTimeOffset; // ms
	public long revolutionTime; // ms
	public long revolutionTimeOffset; // ms

	/**
	 * The radius of the planet in meters.
 	 */
	public double radius; // m
	/**
	 * The minimum height of the planet in meters.
	 * This value must be negative if you want to have oceans.
	 */
	public double minHeight; // m
	/**
	 * The maximum height of the planet in meters.
	 * This value must be positive if you want to have continents and islands.
	 */
	public double maxHeight; // m

	/**
	 * Controls whether the planet has an ocean.
	 */
	public boolean hasOcean;

	/**
	 * The density of craters.
	 * A density 0.0 guarantees no craters.
	 */
	public double craterDensity; // 0.0 - 1.0
	/**
	 * The density of volcanoes.
	 * A density 0.0 guarantees no volcanoes.
	 */
	public double volcanoDensity; // 0.0 - 1.0
	
	/**
	 * Base temperature for calculations in Kelvin.
	 * Not the average temperature.
	 */
	public double baseTemperature; // K

	/**
	 * Base variation that depends on seasonal influences.
	 */
	public double seasonalBaseTemperatureVariation; // dK
	
	/**
	 * Base variation that depends on daily influences.
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

	/**
	 * The height of the atmosphere in meters.
 	 */
	public double atmosphereHeight; // m

	/**
	 * The list of plant data to use during planet creation.
	 */
	public List<PlantData> plants = Collections.emptyList();
	/**
	 * The list of craters to use during planet creation.
	 */
	public List<Crater> craters = Collections.emptyList();
	/**
	 * The list of crater calculators to use during planet creation.
	 */
	public List<CraterCalculator> craterCalculators = Collections.emptyList();

	/**
	 * Influence of the seasonal temperature change to the average temperature.
	 */
	public double seasonTemperatureInfluenceToAverage;
	/**
	 * Influence of the daily temperature change to the average temperature.
	 */
	public double dailyTemperatureInfluenceToAverage;

	/**
	 * Delay angle of the daily temperature to the ocean temperature.
	 * Value between 0 and 2 pi.
	 */
	public double dailyTemperatureOceanDelay;  // 0 .. 2*PI
	/**
	 * Delay angle of the daily temperature to the ground temperature.
	 * Value between 0 and 2 pi.
	 */
	public double dailyTemperatureGroundDelay;  // 0 .. 2*PI
	/**
	 * Factor for the daily temperature change to the ocean temperature.
	 * Value between 0 and 1.
	 * The daily ground temperature has an implicit factor of 1.
	 */
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
