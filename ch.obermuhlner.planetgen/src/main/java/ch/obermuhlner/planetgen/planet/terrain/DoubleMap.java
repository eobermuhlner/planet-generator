package ch.obermuhlner.planetgen.planet.terrain;

public class DoubleMap implements TerrainWriter {

	private final int width;
	private final int height;
	
	private final double[] data;

	public DoubleMap(int width, int height) {
		this.width = width;
		this.height = height;
		
		data = new double[width * height];
	}

	public DoubleMap(int width, int height, double initialValue) {
		this(width, height);
		
		for (int i = 0; i < data.length; i++) {
			data[i] = initialValue;
		}
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setValue(int x, int y, double value) {
		data[x+y*width] = value;
	}
	
	public double getValue(int x, int y) {
		return data[x+y*width];
	}
}
