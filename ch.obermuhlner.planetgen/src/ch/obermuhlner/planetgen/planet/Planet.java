package ch.obermuhlner.planetgen.planet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.layer.Layer;
import ch.obermuhlner.planetgen.planet.layer.PlanetPoint;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

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

	public List<String> getLayerNames() {
		return new ArrayList<>(layers.keySet());
	}
	
	public PlanetPoint getPlanetPoint(double latitude, double longitude, double accuracy) {
		latitude = MathUtil.clamp(latitude, MIN_LATITUDE, MAX_LATITUDE);
		longitude = (longitude - MIN_LONGITUDE) % RANGE_LONGITUDE + MIN_LONGITUDE;
		
		PlanetPoint planetPoint = calculatePlanetPoint(latitude, longitude, accuracy);
		return planetPoint;
	}

	public PlanetTextures getTextures(int textureWidth, int textureHeight) {
		return getTextures(Planet.MIN_LATITUDE, Planet.MAX_LATITUDE, Planet.MIN_LONGITUDE, Planet.MAX_LONGITUDE, textureWidth, textureHeight);
	}
	
	public PlanetTextures getTextures(double fromLatitude, double toLatitude, double fromLongitude, double toLongitude, int textureWidth, int textureHeight) {
		PlanetTextures textures = new PlanetTextures();
		
		WritableImage diffuseTexture = new WritableImage(textureWidth, textureHeight);
		PixelWriter diffuseWriter = diffuseTexture.getPixelWriter();

		WritableImage normalTexture = new WritableImage(textureWidth, textureHeight);
		PixelWriter normalWriter = normalTexture.getPixelWriter();

		WritableImage luminousTexture = new WritableImage(textureWidth, textureHeight);
		PixelWriter luminousWriter = luminousTexture.getPixelWriter();

		double stepLongitude = (toLongitude - fromLongitude) / textureWidth;
		double stepLatitude = (toLatitude - fromLatitude) / textureHeight;

		PlanetPoint[] points = new PlanetPoint[textureWidth * textureHeight];
		
		IntStream.range(0, textureHeight).parallel().forEach(y -> {
			for (int x = 0; x < textureWidth; x++) {
				double longitude = x * stepLongitude + fromLongitude;
				double latitude = y * stepLatitude + fromLatitude;
				
				double accuracy = 1.0;
				PlanetPoint planetPoint = getPlanetPoint(latitude, longitude, accuracy);
				points[x + y * textureWidth] = planetPoint;
			}
		});

		double accuracy = 1;
		IntStream.range(0, textureHeight).parallel().forEach(y -> {
			for (int x = 0; x < textureWidth; x++) {
				double longitude = x * stepLongitude + fromLongitude;
				double latitude = y * stepLatitude + fromLatitude;

				PlanetPoint planetPoint = points[x + y * textureWidth];
				
				// calculate normal color
				double heightDeltaX = 0;
				double heightDeltaY = 0;
				if (planetPoint.height > 0) {
					double heightStepLatitude;
					if (x == 0) {
						heightStepLatitude = getPlanetPoint(latitude - stepLatitude, longitude, accuracy).height;
					} else {
						heightStepLatitude = points[(x-1) + y * textureWidth].height;
					}
					double heightStepLongitude;
					if (y == 0) {
						heightStepLongitude = getPlanetPoint(latitude, longitude - stepLongitude, accuracy).height;
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
				normalWriter.setColor(x, y, new Color(normalColor.x, normalColor.y, normalColor.z, 1.0));

				// diffuse color
				diffuseWriter.setColor(x, y, planetPoint.color);

				// luminous color
				luminousWriter.setColor(x, y, planetPoint.luminousColor);
			}
		});
		
		textures.diffuseTexture = diffuseTexture;
		textures.normalTexture = normalTexture;
		textures.luminousTexture = luminousTexture;
				
		return textures;
	}

	private PlanetPoint calculatePlanetPoint(double latitude, double longitude, double accuracy) {
		PlanetPoint planetPoint = new PlanetPoint();
		
		for (Layer layer : layers.values()) {
			layer.calculatePlanetPoint(planetPoint, planetData, latitude, longitude, accuracy);
		}
		
		return planetPoint;
	}

	public static class PlanetTextures {
		public Image diffuseTexture;
		public Image normalTexture;
		public Image specularTexture;
		public Image luminousTexture;
	}
}
