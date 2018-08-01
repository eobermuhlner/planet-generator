package ch.obermuhlner.planetgen.value;

import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.noise.FractalNoise;

public class NoisePolarValue implements PolarValue {

	private final FractalNoise fractalNoise;
	private double minValue;
	private double maxValue;
	
	public NoisePolarValue(FractalNoise fractalNoise, double minValue, double maxValue){
		this.fractalNoise = fractalNoise;
		this.minValue = minValue;
		this.maxValue = maxValue;
    }

	@Override
	public double polarValue(double angle, double radius, double accuracy) {
    	Vector2 cartesian = Vector2.ofPolar(angle, radius);
    	
    	double relativeAccuracy = accuracy / (maxValue - minValue);
		double noise = fractalNoise.getNoiseWithAccuracy(cartesian.x, cartesian.y, relativeAccuracy) * 0.5 + 0.5;
		double value = (maxValue - minValue) * noise + minValue;
		return value;
	}
} 