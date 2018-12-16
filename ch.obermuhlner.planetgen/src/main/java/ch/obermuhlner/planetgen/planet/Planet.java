package ch.obermuhlner.planetgen.planet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.layer.Layer;
import ch.obermuhlner.planetgen.planet.layer.PlanetPoint;
import ch.obermuhlner.planetgen.planet.texture.TextureType;
import ch.obermuhlner.planetgen.planet.texture.TextureWriter;
import ch.obermuhlner.planetgen.planet.texture.TextureWriterFactory;

/**
 * Generates a planet.
 */
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

	/**
	 * Creates the default planet generation context with all layer types and an accuracy of 10 meters.
	 *
	 * @return the created {@link PlanetGenerationContext}
	 */
	public PlanetGenerationContext createDefaultContext() {
		PlanetGenerationContext context = new PlanetGenerationContext();
		context.layerTypes = getLayerTypes();
		
		context.accuracy = 10.0;
		return context;
	}
	
	public Set<LayerType> getLayerTypes() {
		return layers.keySet();
	}

	/**
	 * Returns information about a specific point on the planet surface.
	 *
	 * @param latitude the latitude in radians (0 to 2 pi)
	 * @param longitude the longitude in radians (0 to pi - 0 is south pole, pi is north pole)
	 * @param context the {@link PlanetGenerationContext}
	 * @return the {@link PlanetPoint} filled with the values specified in the {@code context}
	 */
	public PlanetPoint getPlanetPoint(double latitude, double longitude, PlanetGenerationContext context) {
		latitude = validLatitude(latitude);
		longitude = validLongitude(longitude);
		
		PlanetPoint planetPoint = calculatePlanetPoint(latitude, longitude, context);
		return planetPoint;
	}

	/**
	 * Returns the textures for the entire planet.
	 *
	 * @param textureWidth the width of the generated textures
	 * @param textureHeight the height of the generated textures
	 * @param context the {@link PlanetGenerationContext}
	 * @param textureWriterFactory the {@link TextureWriterFactory}
	 * @param <T> the type of textures to generate
	 * @return the {@link Map} of {@link TextureType} to {@link TextureWriter}
	 */
	public <T> Map<TextureType, TextureWriter<T>> getTextures(int textureWidth, int textureHeight, PlanetGenerationContext context, TextureWriterFactory<T> textureWriterFactory) {
		return getTextures(Planet.MIN_LATITUDE, Planet.MAX_LATITUDE, Planet.MIN_LONGITUDE, Planet.MAX_LONGITUDE, textureWidth, textureHeight, context, textureWriterFactory, null);
	}

	/**
	 * Returns the textures for a part of the planet.
	 *
	 * @param <T> the type of textures to generate
	 * @param fromLatitude the start latitude of the region to generate
	 * @param toLatitude the end latitude of the region to generate
	 * @param fromLongitude the start longitude of the region to generate
	 * @param toLongitude the end longitude of the region to generate
	 * @param textureWidth the width of the generated textures
	 * @param textureHeight the height of the generated textures
	 * @param context the {@link PlanetGenerationContext}
	 * @param textureWriterFactory the {@link TextureWriterFactory}
	 * @param terrainHeightMap the {@link DoubleMap} to be filled with
	 * @return the created {@link Map} of {@link TextureType} to {@link TextureWriter}
	 */
	public <T> Map<TextureType, TextureWriter<T>> getTextures(
			double fromLatitude,
			double toLatitude,
			double fromLongitude,
			double toLongitude,
			int textureWidth,
			int textureHeight,
			PlanetGenerationContext context, TextureWriterFactory<T> textureWriterFactory,
			DoubleMap terrainHeightMap) {
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

		Map<TextureType, TextureWriter<T>> textureWriters = new ConcurrentHashMap<>();

		final TextureWriter<T> diffuseTexture = context.textureTypes.contains(TextureType.DIFFUSE)
				? textureWriters.computeIfAbsent(TextureType.DIFFUSE, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key))
				: null;
		final TextureWriter<T> specularTexture = context.textureTypes.contains(TextureType.SPECULAR)
				? textureWriters.computeIfAbsent(TextureType.SPECULAR, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key))
				: null;
		final TextureWriter<T> luminousTexture = context.textureTypes.contains(TextureType.LUMINOUS)
				? textureWriters.computeIfAbsent(TextureType.LUMINOUS, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key))
				: null;
		final TextureWriter<T> heightTexture = context.textureTypes.contains(TextureType.HEIGHT)
				? textureWriters.computeIfAbsent(TextureType.HEIGHT, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key))
				: null;
		final TextureWriter<T> thermalTexture = context.textureTypes.contains(TextureType.THERMAL)
				? textureWriters.computeIfAbsent(TextureType.THERMAL, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key))
				: null;
		final TextureWriter<T> thermalAverageTexture = context.textureTypes.contains(TextureType.THERMAL_AVERAGE)
				? textureWriters.computeIfAbsent(TextureType.THERMAL_AVERAGE, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key))
				: null;
		final TextureWriter<T> atmosphericPressureTexture = context.textureTypes.contains(TextureType.ATMOSPHERIC_PRESSURE)
				? textureWriters.computeIfAbsent(TextureType.ATMOSPHERIC_PRESSURE, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key))
				: null;
		final TextureWriter<T> precipitationTexture = context.textureTypes.contains(TextureType.PRECIPITATION)
				? textureWriters.computeIfAbsent(TextureType.PRECIPITATION, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key))
				: null;
		final TextureWriter<T> precipitationAverageTexture = context.textureTypes.contains(TextureType.PRECIPITATION_AVERAGE)
				? textureWriters.computeIfAbsent(TextureType.PRECIPITATION_AVERAGE, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key))
				: null;
		final TextureWriter<T> cloudTexture = context.textureTypes.contains(TextureType.CLOUD)
				? textureWriters.computeIfAbsent(TextureType.CLOUD, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key))
				: null;
		final TextureWriter<T> debugTexture = context.textureTypes.contains(TextureType.DEBUG)
				? textureWriters.computeIfAbsent(TextureType.DEBUG, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key))
				: null;

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
					TextureWriter<T> textureWriter = textureWriters.computeIfAbsent(TextureType.NORMAL, (key) -> textureWriterFactory.createTextureWriter(textureWidth, textureHeight, key));
					textureWriter.setColor(x, y, Color.rgb(normalColor.x, normalColor.y, normalColor.z));
				}

				if (diffuseTexture != null) {
					diffuseTexture.setColor(x, y, planetPoint.color);
				}
				if (specularTexture != null) {
					specularTexture.setColor(x, y, planetPoint.specularColor);
				}
				if (luminousTexture != null) {
					luminousTexture.setColor(x, y, planetPoint.luminousColor);
				}
				if (heightTexture != null) {
					heightTexture.setColor(x, y, heightColorScale.toColor(planetPoint.groundHeight));
				}
				if (thermalTexture != null) {
					thermalTexture.setColor(x, y, ColorScale.TEMPERATURE_HUMAN_RANGE.toColor(planetPoint.temperature));
				}
				if (thermalAverageTexture != null) {
					thermalAverageTexture.setColor(x, y, ColorScale.TEMPERATURE_HUMAN_RANGE.toColor(planetPoint.temperatureAverage));
				}
				if (atmosphericPressureTexture != null) {
					atmosphericPressureTexture.setColor(x, y, ColorScale.ATMOSPHERIC_PRESSURE_HUMAN_RANGE.toColor(planetPoint.atmospherePressure));
				}
				if (precipitationTexture != null) {
					precipitationTexture.setColor(x, y, ColorScale.PRECIPITATION_HUMAN_RANGE.toColor(planetPoint.precipitation));
				}
				if (precipitationAverageTexture != null) {
					precipitationAverageTexture.setColor(x, y, ColorScale.PRECIPITATION_HUMAN_RANGE.toColor(planetPoint.precipitationAverage));
				}
				if (cloudTexture != null) {
					cloudTexture.setColor(x, y, toCloudColor(planetPoint.cloud));
				}
				if (debugTexture != null) {
					debugTexture.setColor(x, y, debugColorScale.toColor(planetPoint.debug));
				}
			}
		});

		return textureWriters;
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
