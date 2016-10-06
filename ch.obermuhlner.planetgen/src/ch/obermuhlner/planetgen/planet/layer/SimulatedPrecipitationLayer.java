package ch.obermuhlner.planetgen.planet.layer;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.planet.LayerType;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.util.Units;

public class SimulatedPrecipitationLayer implements Layer {

	private static final PlanetGenerationContext heightContext = new PlanetGenerationContext();
	
	private static final int iterationCount = 30;
	
	private static final double windStrengthFactor = 0.02;
	
	private static final double spontaneousPrecipitationFactor = 0.05;
	
	static {
		heightContext.accuracy = 10;
		heightContext.layers.add(LayerType.GROUND);
		heightContext.layers.add(LayerType.OCEAN);
		heightContext.layers.add(LayerType.TEMPERATURE);
		heightContext.layers.add(LayerType.PREVAILING_WIND);
	}

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		List<PlanetPoint> windPoints = new ArrayList<>();

		windPoints.add(planetPoint); // assumes that planetPoint contains all layers necessary
		double windLength = planetPoint.prevailingWindStrength * windStrengthFactor;
		double windAngle = planetPoint.prevailingWindAngle;
		
		Vector2 pos = Vector2.of(latitude, longitude);
		for (int i = 0; i < iterationCount; i++) {
			Vector2 wind = Vector2.ofPolar(windAngle, windLength);
			pos = pos.add(wind);
			
			PlanetPoint windPoint = planet.getPlanetPoint(pos.x, pos.y, heightContext);
			windPoints.add(windPoint);
			windAngle = windPoint.prevailingWindAngle;
			windLength = windPoint.prevailingWindStrength * windStrengthFactor;
		}
		
		double precipitation = 0;
		double humidity = 0;
		for (int i = 0; i < windPoints.size(); i++) {
			PlanetPoint windPoint = windPoints.get(windPoints.size() - i - 1);
			if (windPoint.isWater) {
				double evaporation = Math.max(0, (windPoint.temperatureAverage - Units.celsiusToKelvin(-20)) / 50);
				//evaporation = evaporation * evaporation;
				humidity += evaporation;
			}

			double maxHumidity = Math.max(0, (windPoint.temperatureAverage - Units.celsiusToKelvin(-20)) / 40) * 2;
			//maxHumidity = maxHumidity * maxHumidity;
			precipitation = humidity > maxHumidity ? humidity - maxHumidity : 0;
			humidity -= precipitation;
			
			double spontaneousPrecipitation = humidity * spontaneousPrecipitationFactor;
			precipitation += spontaneousPrecipitation;
			//humidity -= spontaneousPrecipitation;
		}
		
		planetPoint.precipitation = precipitation;
		planetPoint.precipitationAverage = planetPoint.precipitation;
	}
}
