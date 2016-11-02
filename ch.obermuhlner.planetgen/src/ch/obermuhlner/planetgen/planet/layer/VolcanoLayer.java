package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.SphereValue;

public class VolcanoLayer implements Layer {

	private SphereValue valueFunction;

	public VolcanoLayer(SphereValue valueFunction) {
		this.valueFunction = valueFunction;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		double volcano = valueFunction.sphereValue(latitude, longitude, context);
		
		planetPoint.volcano = volcano;
		planetPoint.debug = volcano;
	}
}
