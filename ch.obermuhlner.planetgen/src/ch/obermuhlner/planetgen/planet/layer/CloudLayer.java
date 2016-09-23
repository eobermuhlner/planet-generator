package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class CloudLayer implements Layer {

	public Height heightFunction;

	public CloudLayer(Height heightFunction) {
		this.heightFunction = heightFunction;
	}

	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {
		double cloud = MathUtil.smoothstep(0.0, 1.0, heightFunction.height(latitude, longitude, accuracy));
		cloud = cloud * cloud;
		double cloudHeight = cloud * planetData.atmosphereHeight;
		
		if (cloudHeight > layerState.height) {
			layerState.height = cloudHeight;
			layerState.color = layerState.color.interpolate(Color.WHITE, cloud);
		}
	}
	
}
