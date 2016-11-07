package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.SphereValue;
import ch.obermuhlner.planetgen.value.Vector2Value;

public class GroundLayer implements Layer {

	private Color deepOceanFloorColor;
	private Color shallowOceanFloorColor;
	private Color lowGroundColor;
	private Color midGroundColor;
	private Color highGroundColor1;
	private Color highGroundColor2;
	
	private SphereValue heightFunction;
	private Vector2Value layerFunction;

	public GroundLayer(Color deepOceanFloorColor, Color shallowOceanFloorColor, Color lowGroundColor, Color midGroundColor, Color highGroundColor1, Color highGroundColor2, SphereValue heightFunction, Vector2Value layerFunction) {
		this.deepOceanFloorColor = deepOceanFloorColor;
		this.shallowOceanFloorColor = shallowOceanFloorColor;
		this.lowGroundColor = lowGroundColor;
		this.midGroundColor = midGroundColor;
		this.highGroundColor1 = highGroundColor1;
		this.highGroundColor2 = highGroundColor2;
		this.heightFunction = heightFunction;
		this.layerFunction = layerFunction;
	}

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		planetPoint.groundHeight = heightFunction.sphereValue(latitude, longitude, context);
		planetPoint.height += planetPoint.groundHeight;

		if (planetPoint.height <= 0) {
			double relativeHeight = planetPoint.height / planet.planetData.minHeight;
			planetPoint.groundColor = shallowOceanFloorColor.interpolate(deepOceanFloorColor, relativeHeight);
			planetPoint.isWater = true;
		} else {
			double relativeHeight = planetPoint.height / planet.planetData.maxHeight;
			if (relativeHeight < 0.5) {
				planetPoint.groundColor = lowGroundColor.interpolate(midGroundColor, relativeHeight * 2.0);
			} else {
				double layerHeight = layerFunction.vector2Value(Vector2.of(1.0, relativeHeight), 0.000001);
				planetPoint.debug = layerHeight;
				Color layeredColor = highGroundColor1.interpolate(highGroundColor2, layerHeight);
				planetPoint.groundColor = layeredColor;
				planetPoint.groundColor = midGroundColor.interpolate(layeredColor, (relativeHeight - 0.5) * 2.0);
			}
		}
		planetPoint.color = planetPoint.groundColor;
	}
}
