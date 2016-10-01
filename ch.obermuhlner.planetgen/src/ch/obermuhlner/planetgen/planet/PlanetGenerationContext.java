package ch.obermuhlner.planetgen.planet;

import java.util.EnumSet;
import java.util.Set;

public class PlanetGenerationContext {

	public Set<String> layers;
	
	public double accuracy;

	public final Set<TextureType> enabledTextureTypes = EnumSet.noneOf(TextureType.class);
}
