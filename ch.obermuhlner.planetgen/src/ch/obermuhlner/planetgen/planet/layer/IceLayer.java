package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import javafx.scene.paint.Color;

public class IceLayer implements Layer {

	private final double oceanIceLevel = 0.9;
	private final double groundIceLevel = 0.9;

	private final double oceanIceThickness = 100; // m 
	private final double groundIceThickness = 2000; // m 
	
	private final Color iceColor;
	

	public IceLayer(Color iceColor) {
		this.iceColor = iceColor;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, double accuracy) {
		double distanceToEquator = relativeDistanceToEquator(latitude);

		double oceanDepth = planetPoint.height < 0 ? planetPoint.height : 0;
		double oceanRelativeDepth = MathUtil.smoothstep(0.0, 1.0, Math.abs(oceanDepth / planetData.minHeight));
		double oceanRelativeTemperature = distanceToEquator - oceanRelativeDepth * 0.05;
		double oceanIce = MathUtil.smoothstep(oceanIceLevel, MathUtil.higher(oceanIceLevel, 1.0, 0.1), oceanRelativeTemperature);
		double oceanIceHeight = oceanIce * oceanIceThickness; 
		
		if (planetPoint.height <= 0) {
			planetPoint.height += oceanIceHeight;
			planetPoint.color = planetPoint.color.interpolate(iceColor, oceanIce);			
		} else {
			double groundRelativeHeight = planetPoint.height / planetData.maxHeight;
			double groundRelativeTemperature = distanceToEquator + groundRelativeHeight * 0.2;
			double groundIce = MathUtil.smoothstep(groundIceLevel, MathUtil.higher(groundIceLevel, 1.0, 0.5), groundRelativeTemperature);
			double groundIceHeight = groundIce * groundIceThickness;
			double iceHeight = groundIceHeight + oceanIceHeight; 
			
			planetPoint.height += iceHeight;
			planetPoint.color = planetPoint.color.interpolate(iceColor, groundIce);
		}
	}

}
