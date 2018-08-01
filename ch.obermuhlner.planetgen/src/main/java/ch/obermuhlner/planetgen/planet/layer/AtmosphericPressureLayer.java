package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.SphereValue;

public class AtmosphericPressureLayer implements Layer {

	private final SphereValue valueFunction;
	
	public AtmosphericPressureLayer(SphereValue valueFunction) {
		this.valueFunction = valueFunction;
	}

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		double pressure = valueFunction.sphereValue(latitude, longitude, context);
		//pressure = MathUtil.smoothstep(0, 1, Math.abs(pressure)) * Math.signum(pressure);
		planetPoint.atmospherePressure = 1.0 + pressure * 0.05;
	}
	
}
