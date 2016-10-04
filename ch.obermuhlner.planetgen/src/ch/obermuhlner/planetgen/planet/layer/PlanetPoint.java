package ch.obermuhlner.planetgen.planet.layer;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer.PlantData;
import ch.obermuhlner.util.Tuple2;

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
	
	public List<Tuple2<PlantData, Double>> plants = new ArrayList<>();
	
	public double city;
	
	public Color groundColor;
	public Color plantColor;
	public Color oceanColor;
	public Color iceColor;
	
	public double debug;
}
