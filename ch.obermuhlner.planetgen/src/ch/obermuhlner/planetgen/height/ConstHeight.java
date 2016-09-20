package ch.obermuhlner.planetgen.height;

public class ConstHeight implements Height {

	private double height;

	public ConstHeight(double height) {
		this.height = height;
	}
	
	@Override
	public double height(double latitude, double longitude, double accuracy) {
		return height;
	}

}
