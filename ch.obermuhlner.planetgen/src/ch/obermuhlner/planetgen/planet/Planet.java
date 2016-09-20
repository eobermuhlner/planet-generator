package ch.obermuhlner.planetgen.planet;

import ch.obermuhlner.planetgen.app.DoubleMap;
import ch.obermuhlner.planetgen.height.Height;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Planet {

	public double radius;
	
	public double minHeight;
	public double maxHeight;
	
	public Height heightFunction;
	
	public double getHeight(double latitude, double longitude, double accuracy) {
		return heightFunction.height(latitude, longitude, accuracy);
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
	
	public PlanetTextures getTextures(double fromLatitude, double toLatitude, double fromLongitude, double toLongitude, int textureWidth, int textureHeight) {
		DoubleMap heightMap = getHeightMap(fromLatitude, toLatitude, fromLongitude, toLongitude, textureWidth, textureHeight);
		
		PlanetTextures textures = new PlanetTextures();
		WritableImage diffuseTexture = new WritableImage(textureWidth, textureHeight);
		
		PixelWriter diffuseWriter = diffuseTexture.getPixelWriter();
		
		for (int y = 0; y < textureHeight; y++) {
			for (int x = 0; x < textureWidth; x++) {
				double h = heightMap.getValue(x, y);
				Color color = heightToColor(h);
				diffuseWriter.setColor(x, y, color);
			}
		}
		textures.diffuseTexture = diffuseTexture;
				
		return textures;
	}
	
	private Color heightToColor(double height) {
		if (height < 0) {
			return Color.DARKBLUE;
		}
		
		return Color.BROWN.interpolate(Color.WHITE, height / maxHeight);
	}

	public static class PlanetTextures {
		public Image diffuseTexture;
		public Image normalTexture;
	}
}
