package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.SphereValue;

public class GroundLayer implements Layer {

	private Color deepOceanFloorColor;
	private Color shallowOceanFloorColor;
	private Color lowGroundColor;
	private Color midGroundColor;
	private Color highCroundColor;
	
	private SphereValue valueFunction;

	public GroundLayer(Color deepOceanFloorColor, Color shallowOceanFloorColor, Color lowGroundColor, Color midGroundColor, Color highCroundColor, SphereValue valueFunction) {
		this.deepOceanFloorColor = deepOceanFloorColor;
		this.shallowOceanFloorColor = shallowOceanFloorColor;
		this.lowGroundColor = lowGroundColor;
		this.midGroundColor = midGroundColor;
		this.highCroundColor = highCroundColor;
		this.valueFunction = valueFunction;
	}

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		planetPoint.groundHeight = valueFunction.polarValue(latitude, longitude, context);
		planetPoint.height += planetPoint.groundHeight;

		if (planetPoint.height <= 0) {
			double relativeHeight = planetPoint.height / planet.planetData.minHeight;
			planetPoint.groundColor = shallowOceanFloorColor.interpolate(deepOceanFloorColor, relativeHeight);
		} else {
			double relativeHeight = planetPoint.height / planet.planetData.maxHeight;
			if (relativeHeight < 0.5) {
				planetPoint.groundColor = lowGroundColor.interpolate(midGroundColor, relativeHeight * 2.0);
			} else {
				planetPoint.groundColor = midGroundColor.interpolate(highCroundColor, (relativeHeight - 0.5) * 2.0);
			}
		}
		planetPoint.color = planetPoint.groundColor; 
	}
}
