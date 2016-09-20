package ch.obermuhlner.planetgen.planet;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector3;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Planet {

	public static final double MIN_LONGITUDE = 0;
	public static final double MAX_LONGITUDE = 2000;

	public static final double MIN_LATITUDE = -1000;
	public static final double MAX_LATITUDE = 1000;

	private static final double RANGE_LONGITUDE = MAX_LONGITUDE - MIN_LONGITUDE;
	private static final double RANGE_LATITUDE = MAX_LATITUDE - MIN_LATITUDE;


	public double radius;
	
	public double minHeight;
	public double maxHeight;
	
	public Height heightFunction;
	
	public double getHeight(double latitude, double longitude, double accuracy) {
		latitude = MathUtil.clamp(latitude, MIN_LATITUDE, MAX_LATITUDE);
		longitude = (longitude - MIN_LONGITUDE) % RANGE_LONGITUDE + MIN_LONGITUDE;
		
		double height1 = heightFunction.height(latitude, longitude, accuracy);
		double height2 = heightFunction.height(latitude, longitude - RANGE_LONGITUDE, accuracy);
		double longitudeWeight = (longitude - MIN_LONGITUDE) / RANGE_LONGITUDE;
		return MathUtil.mix(height1, height2, longitudeWeight);
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
		return getTextures(	Planet.MIN_LATITUDE, Planet.MAX_LATITUDE, Planet.MIN_LONGITUDE, Planet.MAX_LONGITUDE, textureWidth, textureHeight);
	}
	
	public PlanetTextures getTextures(double fromLatitude, double toLatitude, double fromLongitude, double toLongitude, int textureWidth, int textureHeight) {
		PlanetTextures textures = new PlanetTextures();
		
		WritableImage diffuseTexture = new WritableImage(textureWidth, textureHeight);
		PixelWriter diffuseWriter = diffuseTexture.getPixelWriter();

		WritableImage normalTexture = new WritableImage(textureWidth, textureHeight);
		PixelWriter normalWriter = normalTexture.getPixelWriter();

		WritableImage specularTexture = new WritableImage(textureWidth, textureHeight);
		PixelWriter specularWriter = specularTexture.getPixelWriter();

		double stepLongitude = RANGE_LONGITUDE / textureWidth;
		double stepLatitude = RANGE_LATITUDE / textureHeight;

		double deltaLongitude = stepLongitude * 1.0;
		double deltaLatitude = stepLatitude * 1.0;

		for (int y = 0; y < textureHeight; y++) {
			for (int x = 0; x < textureWidth; x++) {
				double longitude = x * stepLongitude + fromLongitude;
				double latitude = y * stepLatitude + fromLatitude;
				
				double accuracy = 1.0;
				double height = getHeight(latitude, longitude, accuracy);

				// calculate diffuse color
				Color diffuseColor = toColor(height, latitude);
				diffuseWriter.setColor(x, y, diffuseColor);
				
				// calculate specular color
				if (height <= 0) {
					specularWriter.setColor(x, y, Color.LIGHTBLUE);
				} else {
					specularWriter.setColor(x, y, Color.BLACK);
				}
				
				// calculate normal color
				double heightDeltaX = 0;
				double heightDeltaY = 0;
				if (height > 0) {
					heightDeltaX = height - getHeight(latitude, longitude + deltaLongitude, accuracy);
					heightDeltaY = height - getHeight(latitude + deltaLatitude, longitude, accuracy);
				}
				Vector3 tangentX = Vector3.of(deltaLongitude, 0, heightDeltaX / -500);
				Vector3 tangentY = Vector3.of(0, deltaLatitude, heightDeltaY / 500);
				Vector3 normal = Vector3.cross(tangentX, tangentY).normalize();
				Vector3 normalColor = normal.add(1.0).divide(2.0).clamp(0.0, 1.0);
				normalWriter.setColor(x, y, new Color(normalColor.x, normalColor.y, normalColor.z, 1.0));
			}
		}
		
		textures.diffuseTexture = diffuseTexture;
		textures.normalTexture = normalTexture;
		//textures.specularTexture = specularTexture;
				
		return textures;
	}
	
	private Color toColor(double height, double latitude) {

		double snow;
		Color groundColor;
		double distanceToEquator = Math.abs(latitude) / MAX_LATITUDE;
		
		if (height < 0) {
			double relativeHeight = Math.abs(height / minHeight);
			groundColor = Color.DARKBLUE;
			snow = MathUtil.smoothstep(0.78, 0.8, distanceToEquator - relativeHeight * 0.1);
		} else {
			double relativeHeight = height / maxHeight;
			groundColor = Color.BROWN.interpolate(Color.GRAY, relativeHeight);
			snow = MathUtil.smoothstep(0.5, 1.0, distanceToEquator + relativeHeight);
		}
		
		
		return groundColor.interpolate(Color.WHITE, snow);
	}

	public static class PlanetTextures {
		public Image diffuseTexture;
		public Image normalTexture;
		public Image specularTexture;
	}
}
