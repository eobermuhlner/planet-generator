package ch.obermuhlner.planetgen.planet;

import java.util.EnumSet;
import java.util.Set;

public class PlanetGenerationContext {

	public Set<LayerType> layers = EnumSet.noneOf(LayerType.class);
	
	public double accuracy;

	public final Set<TextureType> enabledTextureTypes = EnumSet.noneOf(TextureType.class);
}
