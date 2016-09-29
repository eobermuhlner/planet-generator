package ch.obermuhlner.planetgen.height;

import ch.obermuhlner.planetgen.math.Vector3;

public class NoiseHeight implements Height {

	private final FractalNoise fractalNoise;
	private double minHeight;
	private double maxHeight;
	
	public NoiseHeight(FractalNoise fractalNoise, double minHeight, double maxHeight){
		this.fractalNoise = fractalNoise;
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
    }

    @Override
    public double height(double latitude, double longitude, double accuracy) {
    	Vector3 cartesian = Vector3.ofPolar(latitude, longitude, 1.0);
    	
    	double noise = fractalNoise.getNoise(cartesian.x, cartesian.y, cartesian.z) * 0.5 + 0.5;
		double height = (maxHeight - minHeight) * noise + minHeight;
		return height;
    }
} 