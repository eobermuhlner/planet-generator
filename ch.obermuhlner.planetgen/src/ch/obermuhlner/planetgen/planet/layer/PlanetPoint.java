package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.Color;

public class PlanetPoint {

	public double height;

	public Color color = Color.BLACK;
	public Color luminousColor = Color.BLACK;
	public Color specularColor = Color.BLACK;

	public double temperatureAverage;
	public double temperature;
	
	public double precipitationAverage;
	public double precipitation;
	
	public double groundHeight;
	public double iceHeight;
	public double snowHeight;
	
	public boolean isWater;
	
	
	public double city;
	
	public Color groundColor;
	public Color plantColor;
	public Color oceanColor;
	public Color iceColor;
	
	public double debug;
}
