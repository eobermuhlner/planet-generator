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
		//double cloud = MathUtil.smoothstep(0.8, 0.9, valueFunction.sphereValue(latitude, longitude, context));
		double cloud = valueFunction.sphereValue(latitude, longitude, 1.0, 0.0001);

		double oceanPart = planet.planetData.getOceanPart();
		double minEdge = 0.95 - oceanPart * 0.5;
		double maxEdge = 1.0 - oceanPart * 0.1;

		cloud = MathUtil.smoothstep(minEdge, maxEdge, cloud);
		cloud = Math.sqrt(cloud);

		planetPoint.cloud = cloud;
		planetPoint.cloudHeight = planet.planetData.radius + planet.planetData.atmosphereHeight * 0.5;
	}
	
}
