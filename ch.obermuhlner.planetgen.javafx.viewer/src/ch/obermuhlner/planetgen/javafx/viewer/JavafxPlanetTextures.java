package ch.obermuhlner.planetgen.javafx.viewer;

import java.util.HashMap;
import java.util.Map;

import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.PlanetTextures;
import ch.obermuhlner.planetgen.planet.TextureType;
import ch.obermuhlner.planetgen.planet.TextureWriter;
import javafx.scene.image.Image;

public class JavafxPlanetTextures implements PlanetTextures {

	private final Map<TextureType, JavafxTextureWriter> textures = new HashMap<>();
	
	public JavafxPlanetTextures(int textureWidth, int textureHeight, PlanetGenerationContext context) {
		for (TextureType textureType : context.enabledTextureTypes) {
			textures.put(textureType, new JavafxTextureWriter(textureWidth, textureHeight));
		}
	}
	
	@Override
	public TextureWriter getTextureWriter(TextureType textureType) {
		return textures.get(textureType);
	}

	public Image getImage(TextureType textureType) {
		return textures.get(textureType).getImage();
	}
}