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
	
	private static final int iterationCount = 20;
	
	private static final double windStrengthFactor = 0.01;
	
	private static final double spontaneousPrecipitationFactor = 0.05;
	
	static {
		heightContext.accuracy = 10;
		heightContext.layerTypes.add(LayerType.GROUND);
		heightContext.layerTypes.add(LayerType.OCEAN);
		heightContext.layerTypes.add(LayerType.TEMPERATURE);
		heightContext.layerTypes.add(LayerType.PREVAILING_WIND);
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
		double precipitationAverage = 0;
		double humidity = 0;
		double humidityAverage = 0;
		for (int i = 0; i < windPoints.size(); i++) {
			PlanetPoint windPoint = windPoints.get(windPoints.size() - i - 1);
			if (windPoint.isWater) {
				humidity += evaporation(windPoint.temperature);
				humidityAverage += evaporation(windPoint.temperatureAverage);
			}

			precipitation = precipitation(humidity, windPoint.temperature);
			humidity -= precipitation;
			
			precipitationAverage = precipitation(humidityAverage, windPoint.temperatureAverage);
			humidityAverage -= precipitationAverage;
			
			double spontaneousPrecipitation = humidity * spontaneousPrecipitationFactor;
			precipitation += spontaneousPrecipitation;
			//humidity -= spontaneousPrecipitation;
			
			double spontaneousPrecipitationAverage = humidityAverage * spontaneousPrecipitationFactor;
			precipitationAverage += spontaneousPrecipitationAverage;
			//humidityAverage -= spontaneousPrecipitationAverage;
		}
		
		planetPoint.precipitation = precipitation;
		planetPoint.precipitationAverage = precipitationAverage;
	}
	
	private static double evaporation(double temperature) {
		double evaporation = Math.max(0, (temperature - Units.celsiusToKelvin(-20)) / 50);
		//evaporation = evaporation * evaporation;
		return evaporation;		
	}
	
	private static double precipitation(double humidity, double temperature) {
		double maxHumidity = Math.max(0, (temperature - Units.celsiusToKelvin(-20)) / 40) * 2;
		//maxHumidity = maxHumidity * maxHumidity;
		double precipitation = humidity > maxHumidity ? humidity - maxHumidity : 0;
		return precipitation;
	}
}
