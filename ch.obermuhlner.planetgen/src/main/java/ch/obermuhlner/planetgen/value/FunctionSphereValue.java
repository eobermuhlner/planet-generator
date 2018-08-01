package ch.obermuhlner.planetgen.value;

import java.util.function.DoubleUnaryOperator;

public class FunctionSphereValue implements SphereValue {

	private SphereValue decoratedSphereValue;
	private DoubleUnaryOperator function;

	public FunctionSphereValue(SphereValue decoratedSphereValue, DoubleUnaryOperator function) {
		this.decoratedSphereValue = decoratedSphereValue;
		this.function = function;
	}
	
	@Override
	public double sphereValue(double latitude, double longitude, double radius, double accuracy) {
		return function.applyAsDouble(decoratedSphereValue.sphereValue(latitude, longitude, radius, accuracy));
	}

}
