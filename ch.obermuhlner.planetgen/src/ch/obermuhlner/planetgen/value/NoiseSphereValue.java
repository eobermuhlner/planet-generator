package ch.obermuhlner.planetgen.value;

import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.noise.FractalNoise;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

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
    public double polarValue(double latitude, double longitude, PlanetGenerationContext context) {
    	Vector3 cartesian = Vector3.ofPolar(latitude, longitude, 1.0);
    	
    	double accuracy = context.accuracy / (maxValue - minValue);
		double noise = fractalNoise.getNoiseWithAccuracy(cartesian.x, cartesian.y, cartesian.z, accuracy) * 0.5 + 0.5;
		double value = (maxValue - minValue) * noise + minValue;
		return value;
    }
} 