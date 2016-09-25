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
    	double radius = 1.0;
    	double sinLongitude = Math.sin(longitude);
    	double cosLongitude = Math.cos(longitude);
    	double sinLatitude = Math.sin(latitude);
    	double cosLatitude = Math.cos(latitude);
    	
    	double x = radius * cosLongitude * sinLatitude;
    	double y = radius * sinLongitude * sinLatitude;
    	double z = radius * cosLatitude;
    	
    	double noise = fractalNoise.getNoise(x, y, z) * 0.5 + 0.5;
		double height = (maxHeight - minHeight) * noise + minHeight;
		return height;
    }
} 