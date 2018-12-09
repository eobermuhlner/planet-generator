package ch.obermuhlner.planetgen.planet.texture.awt;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.planet.texture.TextureWriter;

import java.awt.image.BufferedImage;

public class BufferedImageTextureWriter implements TextureWriter<BufferedImage> {

    private final BufferedImage image;

    public BufferedImageTextureWriter(int textureWidth, int textureHeight) {
        image = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public BufferedImage getTexture() {
        return image;
    }

    @Override
    public void setColor(int x, int y, Color color) {
        int[] pixel = new int[4];
        pixel[0] = (int) (255 * color.getRed() + 0.5);
        pixel[1] = (int) (255 * color.getGreen() + 0.5);
        pixel[2] = (int) (255 * color.getBlue() + 0.5);
        pixel[3] = (int) (255 * color.getAlpha() + 0.5);
        image.getRaster().setPixel(x, y, pixel);
    }
}
