package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.SphereValue;

public class CloudLayer implements Layer {

	private final SphereValue cloudValueFunction;
	private SphereValue highPressureValueFunction;

	public CloudLayer(SphereValue cloudValueFunction, SphereValue highPressureValueFunction) {
		this.cloudValueFunction = cloudValueFunction;
		this.highPressureValueFunction = highPressureValueFunction;
	}

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		double cloud = cloudValueFunction.sphereValue(latitude, longitude, 1.0, 0.0001);

		double oceanPart = planet.planetData.getOceanPart();
		double minEdge = 0.95 - oceanPart * 0.5;
		double maxEdge = 1.0 - oceanPart * 0.1;

		if (planetPoint.atmospherePressure > 0) {
			cloud = MathUtil.mix(cloud, cloud * highPressureValueFunction.sphereValue(latitude, longitude, 1.0, 0.0001), planetPoint.atmospherePressure);
		}

		cloud = MathUtil.smoothstep(minEdge, maxEdge, cloud);
		cloud = Math.sqrt(cloud);
		
		planetPoint.cloud = cloud;
		planetPoint.cloudHeight = planet.planetData.radius + planet.planetData.atmosphereHeight * 0.5;
	}
	
}
