package ch.obermuhlner.planetgen.javafx.viewer;

import ch.obermuhlner.planetgen.planet.PlanetTextures;
import ch.obermuhlner.planetgen.planet.TextureWriter;
import javafx.scene.image.Image;

public class JavafxPlanetTextures implements PlanetTextures {

	private JavafxTextureWriter diffuse;
	private JavafxTextureWriter normal;
	private JavafxTextureWriter specular;
	private JavafxTextureWriter luminous;

	public JavafxPlanetTextures(int textureWidth, int textureHeight) {
		diffuse = new JavafxTextureWriter(textureWidth, textureHeight);
		normal = new JavafxTextureWriter(textureWidth, textureHeight);
		specular = new JavafxTextureWriter(textureWidth, textureHeight);
		luminous = new JavafxTextureWriter(textureWidth, textureHeight);
	}
	
	@Override
	public TextureWriter getDiffuseTextureWriter() {
		return diffuse;
	}

	@Override
	public TextureWriter getNormalTextureWriter() {
		return normal;
	}

	@Override
	public TextureWriter getSpecularTextureWriter() {
		return specular;
	}

	@Override
	public TextureWriter getLuminousTextureWriter() {
		return luminous;
	}

	public Image getDiffuseImage() {
		return diffuse.getImage();
	}
	
	public Image getNormalImage() {
		return normal.getImage();
	}
	
	public Image getSpecularImage() {
		return specular.getImage();
	}
	
	public Image getLuminousImage() {
		return luminous.getImage();
	}

}
