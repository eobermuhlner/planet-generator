package ch.obermuhlner.planetgen.planet;

import ch.obermuhlner.planetgen.planet.texture.TextureType;

import java.util.EnumSet;
import java.util.Set;

public class PlanetGenerationContext {

	public Set<LayerType> layerTypes = EnumSet.noneOf(LayerType.class);
	
	public Set<TextureType> textureTypes = EnumSet.noneOf(TextureType.class);

	public double accuracy;
	
	public int craterLayerIndex = Integer.MAX_VALUE;

}
