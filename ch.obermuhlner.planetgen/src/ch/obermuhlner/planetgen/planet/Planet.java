package ch.obermuhlner.planetgen.planet;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.layer.Layer;
import ch.obermuhlner.planetgen.planet.layer.PlanetPoint;
import ch.obermuhlner.util.Units;

public class Planet {

	public static final double MIN_LONGITUDE = 0.0;
	public static final double MAX_LONGITUDE = 2 * Math.PI;
	public static final double CENTER_LONGITUDE = Math.PI;
	
	public static final double MIN_LATITUDE = 0.0;
	public static final double MAX_LATITUDE = Math.PI;
	public static final double EQUATOR_LATITUDE = 0.5 * Math.PI;

	public static final double RANGE_LONGITUDE = MAX_LONGITUDE - MIN_LONGITUDE;
	public static final double RANGE_LATITUDE = MAX_LATITUDE - MIN_LATITUDE;

	private static final double NORMAL_FACTOR = 0.000002;
	
	public PlanetData planetData;
	
	public final Map<String, Layer> layers = new LinkedHashMap<>();

	public PlanetGenerationContext createDefaultContext() {
		PlanetGenerationContext context = new PlanetGenerationContext();
		context.layers = getLayerNames();
		context.accuracy = 1.0;
		return context;
	}
	
	public Set<String> getLayerNames() {
		return new LinkedHashSet<>(layers.keySet());
	}
	
	public PlanetPoint getPlanetPoint(double latitude, double longitude, PlanetGenerationContext context) {
		latitude = validLatitude(latitude);
		longitude = validLongitude(longitude);
		
		PlanetPoint planetPoint = calculatePlanetPoint(latitude, longitude, context);
		return planetPoint;
	}

	public void getTextures(int textureWidth, int textureHeight, PlanetGenerationContext context, PlanetTextures planetTextures) {
		getTextures(Planet.MIN_LATITUDE, Planet.MAX_LATITUDE, Planet.MIN_LONGITUDE, Planet.MAX_LONGITUDE, textureWidth, textureHeight, context, planetTextures);
	}
	
	public void getTextures(double fromLatitude, double toLatitude, double fromLongitude, double toLongitude, int textureWidth, int textureHeight, PlanetGenerationContext context, PlanetTextures planetTextures) {
		double stepLongitude = (toLongitude - fromLongitude) / textureWidth;
		double stepLatitude = (toLatitude - fromLatitude) / textureHeight;

		PlanetPoint[] points = new PlanetPoint[textureWidth * textureHeight];
		
		IntStream.range(0, textureHeight).parallel().forEach(y -> {
			for (int x = 0; x < textureWidth; x++) {
				double longitude = x * stepLongitude + fromLongitude;
				double latitude = y * stepLatitude + fromLatitude;
				
				PlanetPoint planetPoint = getPlanetPoint(latitude, longitude, context);
				points[x + y * textureWidth] = planetPoint;
			}
		});

		IntStream.range(0, textureHeight).parallel().forEach(y -> {
			for (int x = 0; x < textureWidth; x++) {
				double longitude = x * stepLongitude + fromLongitude;
				double latitude = y * stepLatitude + fromLatitude;

				PlanetPoint planetPoint = points[x + y * textureWidth];
				
				// calculate normal color
				if (context.enabledTextureTypes.contains(TextureType.NORMAL)) {
					double heightDeltaX = 0;
					double heightDeltaY = 0;
					if (planetPoint.height > 0) {
						double heightStepLatitude;
						if (x == 0) {
							heightStepLatitude = getPlanetPoint(latitude - stepLatitude, longitude, context).height;
						} else {
							heightStepLatitude = points[(x-1) + y * textureWidth].height;
						}
						double heightStepLongitude;
						if (y == 0) {
							heightStepLongitude = getPlanetPoint(latitude, longitude - stepLongitude, context).height;
						} else {
							heightStepLongitude = points[x + (y-1) * textureWidth].height;
						}
						heightDeltaX = planetPoint.height - heightStepLongitude;
						heightDeltaY = planetPoint.height - heightStepLatitude;
					}
					Vector3 tangentX = Vector3.of(-stepLongitude, 0, heightDeltaX * -NORMAL_FACTOR);
					Vector3 tangentY = Vector3.of(0, -stepLatitude, heightDeltaY * NORMAL_FACTOR);
					Vector3 normal = tangentX.cross(tangentY).normalize();
					Vector3 normalColor = normal.add(1.0).divide(2.0).clamp(0.0, 1.0);
					planetTextures.getTextureWriter(TextureType.NORMAL).setColor(x, y, Color.rgb(normalColor.x, normalColor.y, normalColor.z));
				}

				// diffuse color
				if (context.enabledTextureTypes.contains(TextureType.DIFFUSE)) {
					planetTextures.getTextureWriter(TextureType.DIFFUSE).setColor(x, y, planetPoint.color);
				}

				if (context.enabledTextureTypes.contains(TextureType.SPECULAR)) {
					planetTextures.getTextureWriter(TextureType.SPECULAR).setColor(x, y, planetPoint.specularColor);
				}

				// luminous color
				if (context.enabledTextureTypes.contains(TextureType.LUMINOUS)) {
					planetTextures.getTextureWriter(TextureType.LUMINOUS).setColor(x, y, planetPoint.luminousColor);
				}

				// thermal color
				if (context.enabledTextureTypes.contains(TextureType.THERMAL)) {
					planetTextures.getTextureWriter(TextureType.THERMAL).setColor(x, y, convertTemperatureToColor(planetPoint.temperature));
				}

				// thermal average color
				if (context.enabledTextureTypes.contains(TextureType.THERMAL_AVERAGE)) {
					planetTextures.getTextureWriter(TextureType.THERMAL_AVERAGE).setColor(x, y, convertTemperatureToColor(planetPoint.temperatureAverage));
				}

				// precipitation color
				if (context.enabledTextureTypes.contains(TextureType.PRECIPITATION)) {
					planetTextures.getTextureWriter(TextureType.PRECIPITATION).setColor(x, y, convertPrecipitationToColor(planetPoint.precipitation));
				}

			}
		});
	}

	private Color convertPrecipitationToColor(double temperature) {
		return convertTemperatureToColor(temperature, 0.0, 0.5, 1.0);
	}

	private Color convertTemperatureToColor(double temperature) {
		return convertTemperatureToColor(temperature, 220, 290);
	}

	private Color convertTemperatureToColor(double temperature, double minTemperature, double maxTemperature) {
		//double midTemperature = (maxTemperature - minTemperature) / 2 + minTemperature;
		double midTemperature = Units.celsiusToKelvin(0);
		return convertTemperatureToColor(temperature, minTemperature, midTemperature, maxTemperature);
	}
	
	private Color convertTemperatureToColor(double temperature, double minTemperature, double midTemperature, double maxTemperature) {
		Color color;
		if (temperature < midTemperature) {
			double weight = (temperature - minTemperature) / (midTemperature - minTemperature);
			color = Color.AQUA.interpolate(Color.YELLOW, weight);
		} else {
			double weight = (temperature - midTemperature) / (maxTemperature - midTemperature);
			color = Color.YELLOW.interpolate(Color.RED, weight);
		}

		// shows 0 degrees celsius as MAGENTA
		double distanceToZero = MathUtil.smoothstep(0, 1, Math.abs(temperature - Units.celsiusToKelvin(0)) / 0.5); 
		if (distanceToZero < 1.0) {
			color = color.interpolate(Color.MAGENTA, 1.0 - distanceToZero);
		}
		
		return color;
	}
	
	private PlanetPoint calculatePlanetPoint(double latitude, double longitude, PlanetGenerationContext context) {
		PlanetPoint planetPoint = new PlanetPoint();
		
		for (Layer layer : layers.values()) {
			layer.calculatePlanetPoint(planetPoint, planetData, latitude, longitude, context);
		}
		
		return planetPoint;
	}

	public static double validLatitude(double latitude) {
		return MathUtil.clamp(latitude, MIN_LATITUDE, MAX_LATITUDE);
	}
	
	public static double validLongitude(double longitude) {
		return (longitude - MIN_LONGITUDE) % RANGE_LONGITUDE + MIN_LONGITUDE;
	}
}
