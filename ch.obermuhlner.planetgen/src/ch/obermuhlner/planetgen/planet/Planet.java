package ch.obermuhlner.planetgen.planet;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.layer.Layer;
import ch.obermuhlner.planetgen.planet.layer.LayerState;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Planet {

	public static final double MIN_LONGITUDE = 0.0;
	public static final double MAX_LONGITUDE = 360.0;

	public static final double MIN_LATITUDE = -90.0;
	public static final double MAX_LATITUDE = 90.0;

	public static final double RANGE_LONGITUDE = MAX_LONGITUDE - MIN_LONGITUDE;
	public static final double RANGE_LATITUDE = MAX_LATITUDE - MIN_LATITUDE;

	/*
	 * planet layers in order of rendering:
	 * - ground
	 * - water
	 * - ice
	 * - craters
	 * - buildings
	 * - plants
	 * - clouds
	 */

	public PlanetData planetData;
	
	public final List<Layer> layers = new ArrayList<>();

	public double getHeight(double latitude, double longitude, double accuracy) {
		latitude = MathUtil.clamp(latitude, MIN_LATITUDE, MAX_LATITUDE);
		longitude = (longitude - MIN_LONGITUDE) % RANGE_LONGITUDE + MIN_LONGITUDE;
		
		LayerState layerState = getLayerState(latitude, longitude, accuracy);
		return layerState.height;
	}

	public DoubleMap getHeightMap(double fromLatitude, double toLatitude, double fromLongitude, double toLongitude, int mapWidth, int mapHeight) {
		DoubleMap heightMap = new DoubleMap(mapWidth, mapHeight);

		double rangeLongitude = toLongitude - fromLongitude;
		double rangeLatitude = toLatitude - fromLatitude;
		
		for (int y = 0; y < mapHeight; y++) {
			for (int x = 0; x < mapWidth; x++) {
				double longitude = x * rangeLongitude + fromLongitude;
				double latitude = y * rangeLatitude + fromLatitude;
				
				double accuracy = 1.0;
				double height = getHeight(latitude, longitude, accuracy);
				heightMap.setValue(x, y, height);
			}
		}
		
		return heightMap;
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

		double stepLongitude = RANGE_LONGITUDE / textureWidth;
		double stepLatitude = RANGE_LATITUDE / textureHeight;

		double deltaLongitude = stepLongitude * 1.0;
		double deltaLatitude = stepLatitude * 1.0;

		for (int y = 0; y < textureHeight; y++) {
			for (int x = 0; x < textureWidth; x++) {
				double longitude = x * stepLongitude + fromLongitude;
				double latitude = y * stepLatitude + fromLatitude;
				
				double accuracy = 1.0;
				LayerState layerState = getLayerState(latitude, longitude, accuracy);

				// calculate normal color
				double heightDeltaX = 0;
				double heightDeltaY = 0;
				if (layerState.height > 0) {
					heightDeltaX = layerState.height - getHeight(latitude, longitude + deltaLongitude, accuracy);
					heightDeltaY = layerState.height - getHeight(latitude + deltaLatitude, longitude, accuracy);
				}
				Vector3 tangentX = Vector3.of(deltaLongitude, 0, heightDeltaX / -500);
				Vector3 tangentY = Vector3.of(0, deltaLatitude, heightDeltaY / 500);
				Vector3 normal = tangentX.cross(tangentY).normalize();
				Vector3 normalColor = normal.add(1.0).divide(2.0).clamp(0.0, 1.0);
				normalWriter.setColor(x, y, new Color(normalColor.x, normalColor.y, normalColor.z, 1.0));

				// diffuse color
				diffuseWriter.setColor(x, y, layerState.color);

				// luminous color
				luminousWriter.setColor(x, y, layerState.luminousColor);
			}
		}
		
		textures.diffuseTexture = diffuseTexture;
		textures.normalTexture = normalTexture;
		textures.luminousTexture = luminousTexture;
				
		return textures;
	}

	private LayerState getLayerState(double latitude, double longitude, double accuracy) {
		LayerState layerState = new LayerState();
		
		for (Layer layer : layers) {
			layer.calculateLayerState(layerState, planetData, latitude, longitude, accuracy);
		}
		
		return layerState;
	}

	public static class PlanetTextures {
		public Image diffuseTexture;
		public Image normalTexture;
		public Image specularTexture;
		public Image luminousTexture;
	}
}
