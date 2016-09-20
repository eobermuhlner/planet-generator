package ch.obermuhlner.planetgen.height;

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
    	double noise = fractalNoise.getNoise(latitude, longitude) * 0.5 + 0.5;
		double height = (maxHeight - minHeight) * noise + minHeight;
		return height;
    }
} 