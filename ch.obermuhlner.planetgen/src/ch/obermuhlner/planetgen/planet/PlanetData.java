package ch.obermuhlner.planetgen.planet;

public class PlanetData {

	public double radius; // m
	public double minHeight; // m
	public double maxHeight; // m
	
	public boolean hasOcean;
	
	/**
	 * Base temperature for calculations.
	 * Not the average temperature.
	 */
	public double temperature; // K
	
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
}
