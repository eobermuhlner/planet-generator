package ch.obermuhlner.planetgen.planet.layer;

import ch.obermuhlner.planetgen.height.Height;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.planet.PlanetPhysics;
import ch.obermuhlner.planetgen.math.Color;

public class CityLayer implements Layer {

	private final Color cityGroundColor;
	private final Color cityLightColor;
	private final Height heightFunction;
	
	private final double temperatureCityDelta = 3;

	public CityLayer(Color cityGroundColor, Color cityLightColor, Height heightFunction) {
		this.cityGroundColor = cityGroundColor;
		this.cityLightColor = cityLightColor;
		this.heightFunction = heightFunction;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context) {
		if (planetPoint.height > 0) {
			double city = MathUtil.smoothstep(0.0, 1.0, heightFunction.height(latitude, longitude, context));

			double distanceToEquator = PlanetPhysics.relativeDistanceEquator(latitude);
			double relativeHeight = planetPoint.height / planetData.maxHeight;
			double temperature = distanceToEquator * 0.5 + relativeHeight * 4;
			double climate = 1.0 - MathUtil.smoothstep(0.1, 0.8, temperature);
			
			city *= climate;
			
			planetPoint.temperature += city * temperatureCityDelta;
			
			planetPoint.color = planetPoint.color.interpolate(cityGroundColor, city);
			planetPoint.luminousColor = planetPoint.luminousColor.interpolate(cityLightColor, city * 0.3); 
		}
	}

}
