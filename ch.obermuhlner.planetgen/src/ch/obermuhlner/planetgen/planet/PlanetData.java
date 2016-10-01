package ch.obermuhlner.planetgen.planet;

public class PlanetData {

	public double radius; // m
	public double minHeight; // m
	public double maxHeight; // m
	
	/**
	 * Base temperature for calculations.
	 * Not the average temperature.
	 */
	public double temperature; // K
	
	/**
	 * See https://en.wikipedia.org/wiki/Lapse_rate
	 */
	public double temperatureHeightLapseRate; // -K/m

	/**
	 * Temperature difference between equator and pole.
	 * http://earth.usc.edu/~stott/Catalina/tempdistribution.html
	 */
	public double temperatureLatitudeLapseRate; // -K/90degree
	
	public double atmosphereHeight; // m
}
