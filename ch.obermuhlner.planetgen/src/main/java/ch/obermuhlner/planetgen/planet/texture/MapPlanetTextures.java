package ch.obermuhlner.planetgen.planet.texture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class MapPlanetTextures<T extends TextureWriter> implements PlanetTextures {
    private final Map<TextureType, T> textures = new ConcurrentHashMap<>();
    private final Supplier<T> textureWriterFactory;

    public MapPlanetTextures(Supplier<T> textureWriterFactory) {
        this.textureWriterFactory = textureWriterFactory;
    }

    @Override
    public T getTextureWriter(TextureType textureType) {
        return textures.computeIfAbsent(textureType, key -> textureWriterFactory.get());
    }
}
