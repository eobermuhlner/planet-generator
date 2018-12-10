package ch.obermuhlner.planetgen.planet.layer;

import java.util.List;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.planet.layer.PlantLayer.PlantData;
import ch.obermuhlner.util.Tuple2;

/**
 * Generated values for a concrete point on the planet.
 */
public class PlanetPoint {

	/**
	 * Total height of the surface (including ground, ocean, ice, snow, ...).
	 */
	public double height;

	/**
	 * Total color of the surface (including ground, ocean, ice, snow, ...).
	 */
	public Color color = Color.BLACK;
	/**
	 * Luminous color of the surface.
	 */
	public Color luminousColor = Color.BLACK;
	/**
	 * Specular color of the surface.
	 */
	public Color specularColor = Color.BLACK;

	/**
	 * Average temperature over daily and seasonal variations in Kelvin.
	 */
	public double temperatureAverage;
	/**
	 * Temperature in Kelvin.
	 */
	public double temperature;

	/**
	 * Prevailing wind angle in radians.
	 */
	public double prevailingWindAngle;
	/**
	 * Prevailing wind strength.
	 */
	public double prevailingWindStrength;

	/**
	 * Atmospheric pressure relative to normal pressure at ocean level.
	 * Value is centered around 1.0
	 */
	public double atmospherePressure;

	/**
	 * Average precipitation over daily and seasonal variations.
	 * Value between 0 and 1.
	 */
	public double precipitationAverage;
	/**
	 * Precipitation.
	 * Value between 0 and 1.
	 */
	public double precipitation;

	/**
	 * Cloud thickness.
	 * Value between 0 and 1.
	 */
	public double cloud;

	/**
	 * Height of the ground in meters.
	 */
	public double groundHeight;
	/**
	 * Height of the reef layer in meters.
	 */
	public double reefHeight;
	/**
	 * Height of the ice layer in meters.
	 */
	public double iceHeight;
	/**
	 * Height of the snow sheet in meters.
	 */
	public double snowHeight;

	/**
	 * Minimum height of cloud layer in meters.
	 */
	public double cloudMinHeight;
	/**
	 * Maximum height of cloud layer in meters.
	 */
	public double cloudMaxHeight;

	/**
	 * Water.
	 */
	public boolean isWater;

	/**
	 * Density of plant types.
	 * Value between 0 and 1.
	 */
	public List<Tuple2<PlantData, Double>> plants;

	/**
	 * Density of city construction.
	 * Value between 0 and 1.
	 */
	public double city;

	/**
	 * Color of the ground (without any other layers).
	 */
	public Color groundColor;
	/**
	 * Color of the plant layer.
	 */
	public Color plantColor;
	/**
	 * Color of the ocean layer.
	 */
	public Color oceanColor;
	/**
	 * Color of the ice layer.
	 */
	public Color iceColor;

	/**
	 * Value for debugging.
	 */
	public double debug;

}
