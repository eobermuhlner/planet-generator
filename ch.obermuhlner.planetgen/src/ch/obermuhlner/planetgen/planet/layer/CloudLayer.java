package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class CloudLayer implements Layer {

	private Color cloudColor;
	private final Height heightFunction;
	
	public CloudLayer(Color cloudColor, Height heightFunction) {
		this.cloudColor = cloudColor;
		this.heightFunction = heightFunction;
	}

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, double accuracy) {
		double cloud = MathUtil.smoothstep(0.0, 1.0, heightFunction.height(latitude, longitude, accuracy));
		cloud = cloud * cloud;
		double cloudHeight = cloud * planetData.atmosphereHeight;
		
		if (cloudHeight > planetPoint.height) {
			planetPoint.height = cloudHeight;
			planetPoint.color = planetPoint.color.interpolate(cloudColor, cloud);
		}
	}
	
}
