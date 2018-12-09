package ch.obermuhlner.planetgen.javafx.viewer;

import java.util.HashMap;
import java.util.Map;

import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.texture.MapPlanetTextures;
import ch.obermuhlner.planetgen.planet.texture.PlanetTextures;
import ch.obermuhlner.planetgen.planet.texture.TextureType;
import ch.obermuhlner.planetgen.planet.texture.TextureWriter;
import javafx.scene.image.Image;

public class JavafxPlanetTextures extends MapPlanetTextures<JavafxTextureWriter> {

	public JavafxPlanetTextures(int textureWidth, int textureHeight, PlanetGenerationContext context) {
		super(() -> new JavafxTextureWriter(textureWidth, textureHeight));
	}
	
	public Image getImage(TextureType textureType) {
		return getTextureWriter(textureType).getImage();
	}
}