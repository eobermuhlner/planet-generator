package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.math.Color;

public class CloudLayer implements Layer {

	private Color cloudColor;
	private final Height heightFunction;
	
	public CloudLayer(Color cloudColor, Height heightFunction) {
		this.cloudColor = cloudColor;
		this.heightFunction = heightFunction;
	}

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		double cloud = MathUtil.smoothstep(0.0, 1.0, heightFunction.height(latitude, longitude, context));
		cloud = cloud * cloud;
		double cloudHeight = cloud * planet.planetData.atmosphereHeight;
		
		if (cloudHeight > planetPoint.height) {
			planetPoint.height = cloudHeight;
			planetPoint.color = planetPoint.color.interpolate(cloudColor, cloud);
		}
	}
	
}
