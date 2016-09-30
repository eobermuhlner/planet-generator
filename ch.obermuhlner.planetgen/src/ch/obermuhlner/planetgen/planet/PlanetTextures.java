package ch.obermuhlner.planetgen.planet;

public interface PlanetTextures {
	
	TextureWriter getDiffuseTextureWriter();
	TextureWriter getNormalTextureWriter();
	TextureWriter getSpecularTextureWriter();
	TextureWriter getLuminousTextureWriter();
	
}