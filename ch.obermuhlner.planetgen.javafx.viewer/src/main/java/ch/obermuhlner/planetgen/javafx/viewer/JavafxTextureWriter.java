package ch.obermuhlner.planetgen.javafx.viewer;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.planet.texture.TextureWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class JavafxTextureWriter implements TextureWriter<Image> {
	
	private final WritableImage image;
	private final PixelWriter writer;

	public JavafxTextureWriter(int textureWidth, int textureHeight) {
		image = new WritableImage(textureWidth, textureHeight);
		writer = image.getPixelWriter();
	}
	
	public Image getTexture() {
		return image;
	}

	@Override
	public void setColor(int x, int y, Color color) {
		writer.setColor(x, y, ColorUtil.toJavafxColor(color));
	}
	
}
