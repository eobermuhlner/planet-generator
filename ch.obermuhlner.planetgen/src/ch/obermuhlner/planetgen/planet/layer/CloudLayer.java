package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.SphereValue;

public class CloudLayer implements Layer {

	private final SphereValue valueFunction;
	
	public CloudLayer(SphereValue valueFunction) {
		this.valueFunction = valueFunction;
	}

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		double cloud = MathUtil.smoothstep(0.8, 0.9, valueFunction.sphereValue(latitude, longitude, context));

		planetPoint.cloud = Math.max(cloud, MathUtil.smoothstep(0.01, 0.1, planetPoint.precipitation));
		planetPoint.cloudHeight = planet.planetData.radius + planet.planetData.atmosphereHeight * 0.5;
	}
	
}
