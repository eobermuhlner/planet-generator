package ch.obermuhlner.planetgen.planet.texture;

public interface TextureWriterFactory<T> {
    TextureWriter<T> createTextureWriter(int width, int height, TextureType textureType);
}
