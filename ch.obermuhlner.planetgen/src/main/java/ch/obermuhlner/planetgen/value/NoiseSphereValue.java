package ch.obermuhlner.planetgen.value;

import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.noise.FractalNoise;

public class NoiseSphereValue implements SphereValue {

	private final FractalNoise fractalNoise;
	private double minValue;
	private double maxValue;
	
	public NoiseSphereValue(FractalNoise fractalNoise, double minValue, double maxValue){
		this.fractalNoise = fractalNoise;
		this.minValue = minValue;
		this.maxValue = maxValue;
    }

    @Override
    public double sphereValue(double latitude, double longitude, double radius, double accuracy) {
    	Vector3 cartesian = Vector3.ofPolar(latitude, longitude, radius);
    	
    	double relativeAccuracy = accuracy / (maxValue - minValue);
		double noise = fractalNoise.getNoiseWithAccuracy(cartesian.x, cartesian.y, cartesian.z, relativeAccuracy) * 0.5 + 0.5;
		double value = (maxValue - minValue) * noise + minValue;
		return value;
    }
} 