package ch.obermuhlner.planetgen.value;

import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.noise.FractalNoise;

public class NoiseVector2Value implements Vector2Value {

	private final FractalNoise fractalNoise;
	private double minValue;
	private double maxValue;
	
	public NoiseVector2Value(FractalNoise fractalNoise, double minValue, double maxValue){
		this.fractalNoise = fractalNoise;
		this.minValue = minValue;
		this.maxValue = maxValue;
    }

    @Override
    public double vector2Value(Vector2 value, double accuracy) {
    	double relativeAccuracy = accuracy / (maxValue - minValue);
		double noise = fractalNoise.getNoiseWithAccuracy(value.x, value.y, relativeAccuracy) * 0.5 + 0.5;
		double result = (maxValue - minValue) * noise + minValue;
		return result;
    }
} 