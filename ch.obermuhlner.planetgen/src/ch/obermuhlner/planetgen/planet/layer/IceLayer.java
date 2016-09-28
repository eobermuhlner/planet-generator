package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class IceLayer implements Layer {

	private final double oceanIceLevel = 0.9;
	private final double groundIceLevel = 0.9;

	private final double oceanIceThickness = 100; // m 
	private final double lowGroundIceThickness = 2000; // m
	private final double highGroundIceThickness = 3; // m

	private final double groundOceanLevelTransitionHeight = 1000; // m	

	private final double transparentIceThickness = 2; // m
	
	private final Color iceColor;
	

	public IceLayer(Color iceColor) {
		this.iceColor = iceColor;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, double accuracy) {
		double distanceToEquator = relativeDistanceToEquator(latitude);

		double oceanDepth = planetPoint.groundHeight < 0 ? -planetPoint.groundHeight : 0;
		double oceanRelativeDepth = MathUtil.smoothstep(0.0, 1.0, oceanDepth / -planetData.minHeight);
		double oceanRelativeTemperature = distanceToEquator - oceanRelativeDepth * 0.2;
		double oceanIce = MathUtil.smoothstep(oceanIceLevel, MathUtil.higher(oceanIceLevel, 1.0, 0.1), oceanRelativeTemperature);
		double oceanIceHeight = oceanIce * oceanIceThickness; 
		
		planetPoint.iceColor = iceColor;

		if (planetPoint.groundHeight <= 0) {
			planetPoint.iceHeight = oceanIceHeight;
			planetPoint.height += oceanIceHeight;
			planetPoint.color = planetPoint.color.interpolate(iceColor, MathUtil.smoothstep(0, transparentIceThickness, oceanIceHeight));			
		} else {
			double groundRelativeHeight = planetPoint.height / planetData.maxHeight;
			
			double lowGroundRelativeTemperature = distanceToEquator + groundRelativeHeight * 0.01;
			double lowGroundIce = MathUtil.smoothstep(groundIceLevel, MathUtil.higher(groundIceLevel, 1.0, 0.8), lowGroundRelativeTemperature);
			double lowGroundIceHeight = lowGroundIce * lowGroundIceThickness * MathUtil.smoothstep(0, groundOceanLevelTransitionHeight, planetPoint.height);
			
			double highGroundRelativeTemperature = distanceToEquator + groundRelativeHeight * 0.3;
			double highGroundIce = MathUtil.smoothstep(groundIceLevel, MathUtil.higher(groundIceLevel, 1.0, 0.8), highGroundRelativeTemperature);
			double highGroundIceHeight = highGroundIce * highGroundIceThickness * MathUtil.smoothstep(0, groundOceanLevelTransitionHeight, planetPoint.height);
			
			double iceHeight = oceanIceHeight + lowGroundIceHeight + highGroundIceHeight; 
			
			planetPoint.iceHeight = iceHeight;
			planetPoint.height += iceHeight;
			planetPoint.color = planetPoint.color.interpolate(iceColor, MathUtil.smoothstep(0, transparentIceThickness, iceHeight));			
		}
	}

}
