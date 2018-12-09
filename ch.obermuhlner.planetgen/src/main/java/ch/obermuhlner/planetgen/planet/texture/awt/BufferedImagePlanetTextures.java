package ch.obermuhlner.planetgen.planet.texture.awt;

import ch.obermuhlner.planetgen.planet.texture.MapPlanetTextures;
import ch.obermuhlner.planetgen.planet.texture.TextureType;

import java.awt.image.BufferedImage;

public class BufferedImagePlanetTextures extends MapPlanetTextures<BufferedImageTextureWriter> {
    public BufferedImagePlanetTextures(int textureWidth, int textureHeight) {
        super(() -> new BufferedImageTextureWriter(textureWidth, textureHeight));
    }

    public BufferedImage getImage(TextureType textureType) {
        return getTextureWriter(textureType).getImage();
    }
}
