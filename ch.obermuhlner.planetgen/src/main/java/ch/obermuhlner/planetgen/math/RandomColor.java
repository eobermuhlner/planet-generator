package ch.obermuhlner.planetgen.math;

import ch.obermuhlner.util.Random;

public class RandomColor {
    public static Color random(Random random, Color color1, Color color2) {
        return color1.interpolate(color2, random.nextDouble());
    }

    public static Color random(Random random, Color color1, Color color2, Color color3) {
        return random(random, random(random, color1, color2), color3);
    }

    public static Color random(Random random, Color color1, Color color2, Color color3, Color color4) {
        return random(random, random(random, color1, color2), random(random, color3, color4));
    }
}
