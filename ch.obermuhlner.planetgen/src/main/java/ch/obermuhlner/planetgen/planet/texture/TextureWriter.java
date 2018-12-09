package ch.obermuhlner.planetgen.planet.texture;

import ch.obermuhlner.planetgen.math.Color;

public interface TextureWriter<T> {
	void setColor(int x, int y, Color color);

	T getTexture();
}
