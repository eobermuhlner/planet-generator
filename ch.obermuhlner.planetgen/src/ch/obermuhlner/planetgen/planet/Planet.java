package ch.obermuhlner.planetgen.planet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.IntStream;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.layer.Layer;
import ch.obermuhlner.planetgen.planet.layer.PlanetPoint;

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
	
	public final Map<LayerType, Layer> layers = new LinkedHashMap<>();

	public PlanetGenerationContext createDefaultContext() {
		PlanetGenerationContext context = new PlanetGenerationContext();
		context.layerTypes = getLayerTypes();
		
		context.accuracy = 10.0;
		return context;
	}
	
	public Set<LayerType> getLayerTypes() {
		return layers.keySet();
	}
	
	public PlanetPoint getPlanetPoint(double latitude, double longitude, PlanetGenerationContext context) {
		latitude = validLatitude(latitude);
		longitude = validLongitude(longitude);
		
		PlanetPoint planetPoint = calculatePlanetPoint(latitude, longitude, context);
		return planetPoint;
	}

	public void getTextures(int textureWidth, int textureHeight, PlanetGenerationContext context, PlanetTextures planetTextures) {
		getTextures(Planet.MIN_LATITUDE, Planet.MAX_LATITUDE, Planet.MIN_LONGITUDE, Planet.MAX_LONGITUDE, textureWidth, textureHeight, planetTextures, null, context);
	}
	
	public void getTextures(
			double fromLatitude,
			double toLatitude,
			double fromLongitude,
			double toLongitude,
			int textureWidth,
			int textureHeight,
			PlanetTextures planetTextures,
			DoubleMap terrainHeightMap,
			PlanetGenerationContext context) {
		double stepLongitude = (toLongitude - fromLongitude) / textureWidth;
		double stepLatitude = (toLatitude - fromLatitude) / textureHeight;

		final int terrainWidthStepFactor = terrainHeightMap != null ? textureWidth / terrainHeightMap.width : 0;
		final int terrainHeightStepFactor = terrainHeightMap != null ? textureHeight / terrainHeightMap.height : 0;
		
		PlanetPoint[] points = new PlanetPoint[textureWidth * textureHeight];
		
		IntStream.range(0, textureHeight).parallel().forEach(y -> {
			for (int x = 0; x < textureWidth; x++) {
				double longitude = x * stepLongitude + fromLongitude;
				double latitude = y * stepLatitude + fromLatitude;
				
				PlanetPoint planetPoint = getPlanetPoint(latitude, longitude, context);
				points[x + y * textureWidth] = planetPoint;
				
				if (terrainHeightMap != null) {
					if (x % terrainWidthStepFactor == 0 && y % terrainHeightStepFactor == 0) {
						terrainHeightMap.setValue(x / terrainWidthStepFactor, y / terrainHeightStepFactor, planetPoint.height);
					}
				}
			}
		});

		ColorScale heightColorScale = ColorScale.divergingScale(planetData.minHeight, 0, planetData.maxHeight);
		ColorScale debugColorScale = ColorScale.divergingScale(-1.0, 0, 1.0);
		
		IntStream.range(0, textureHeight).parallel().forEach(y -> {
			for (int x = 0; x < textureWidth; x++) {
				double longitude = x * stepLongitude + fromLongitude;
				double latitude = y * stepLatitude + fromLatitude;

				PlanetPoint planetPoint = points[x + y * textureWidth];
				
				// calculate normal color
				if (context.textureTypes.contains(TextureType.NORMAL)) {
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
				if (context.textureTypes.contains(TextureType.DIFFUSE)) {
					planetTextures.getTextureWriter(TextureType.DIFFUSE).setColor(x, y, planetPoint.color);
				}

				if (context.textureTypes.contains(TextureType.SPECULAR)) {
					planetTextures.getTextureWriter(TextureType.SPECULAR).setColor(x, y, planetPoint.specularColor);
				}

				// luminous color
				if (context.textureTypes.contains(TextureType.LUMINOUS)) {
					planetTextures.getTextureWriter(TextureType.LUMINOUS).setColor(x, y, planetPoint.luminousColor);
				}

				// height color
				if (context.textureTypes.contains(TextureType.HEIGHT)) {
					planetTextures.getTextureWriter(TextureType.HEIGHT).setColor(x, y, heightColorScale.toColor(planetPoint.groundHeight));
				}

				// thermal color
				if (context.textureTypes.contains(TextureType.THERMAL)) {
					planetTextures.getTextureWriter(TextureType.THERMAL).setColor(x, y, ColorScale.TEMPERATURE_HUMAN_RANGE.toColor(planetPoint.temperature));
				}

				// thermal average color
				if (context.textureTypes.contains(TextureType.THERMAL_AVERAGE)) {
					planetTextures.getTextureWriter(TextureType.THERMAL_AVERAGE).setColor(x, y, ColorScale.TEMPERATURE_HUMAN_RANGE.toColor(planetPoint.temperatureAverage));
				}

				// atmospheric pressure color
				if (context.textureTypes.contains(TextureType.ATMOSPHERIC_PRESSURE)) {
					planetTextures.getTextureWriter(TextureType.ATMOSPHERIC_PRESSURE).setColor(x, y, ColorScale.ATMOSPHERIC_PRESSURE_HUMAN_RANGE.toColor(planetPoint.atmospherePressure));
				}

				// precipitation color
				if (context.textureTypes.contains(TextureType.PRECIPITATION)) {
					planetTextures.getTextureWriter(TextureType.PRECIPITATION).setColor(x, y, ColorScale.PRECIPITATION_HUMAN_RANGE.toColor(planetPoint.precipitation));
				}

				// precipitation average color
				if (context.textureTypes.contains(TextureType.PRECIPITATION_AVERAGE)) {
					planetTextures.getTextureWriter(TextureType.PRECIPITATION_AVERAGE).setColor(x, y, ColorScale.PRECIPITATION_HUMAN_RANGE.toColor(planetPoint.precipitationAverage));
				}
				
				// cloud color
				if (context.textureTypes.contains(TextureType.CLOUD)) {
					planetTextures.getTextureWriter(TextureType.CLOUD).setColor(x, y, toCloudColor(planetPoint.cloud));
				}
				
				// debug color
				if (context.textureTypes.contains(TextureType.DEBUG)) {
					planetTextures.getTextureWriter(TextureType.DEBUG).setColor(x, y, debugColorScale.toColor(planetPoint.debug));
				}
			}
		});
	}

	public static Color toCloudColor(double cloud) {
		return Color.rgb(1.0, 1.0, 1.0, cloud);
	}

	private PlanetPoint calculatePlanetPoint(double latitude, double longitude, PlanetGenerationContext context) {
		PlanetPoint planetPoint = new PlanetPoint();
		
		for (Entry<LayerType, Layer> entry : layers.entrySet()) {
			if (context.layerTypes.contains(entry.getKey())) {
				entry.getValue().calculatePlanetPoint(planetPoint, this, latitude, longitude, context);
			}
		}
		
		return planetPoint;
	}

	public static double validLatitude(double latitude) {
		return MathUtil.clamp(latitude, MIN_LATITUDE, MAX_LATITUDE);
	}
	
	public static double validLongitude(double longitude) {
		if (longitude < 0) {
			longitude += RANGE_LONGITUDE;
		}
		return (longitude - MIN_LONGITUDE) % RANGE_LONGITUDE + MIN_LONGITUDE;
	}
}
