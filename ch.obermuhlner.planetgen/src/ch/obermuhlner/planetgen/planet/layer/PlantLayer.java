package ch.obermuhlner.planetgen.planet.layer;

import java.util.List;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.util.Tuple2;

public class PlantLayer implements Layer {

	private List<PlantData> plantDatas;

	public PlantLayer(List<PlantData> plantDatas) {
		this.plantDatas = plantDatas;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context) {
		if (planetPoint.isWater || planetPoint.iceHeight > 0) {
			planetPoint.plantColor = null;
			return;
		}

		if (!plantDatas.isEmpty()) {
			planetPoint.plantColor = Color.BLACK;
		}
		
		for (PlantData plantData : plantDatas) {
			double temperatureDistance = deviationDistance(planetPoint.temperature, plantData.temperatureOptimum, plantData.temperatureMinusDeviation, plantData.temperaturePlusDeviation);
			double precipitationDistance = deviationDistance(planetPoint.precipitation, plantData.precipitationOptimum, plantData.precipitationMinusDeviation, plantData.precipitationPlusDeviation); 

			double plant = 1.0;
			plant *= 1.0 - MathUtil.smoothstep(0, 1, temperatureDistance);
			plant *= 1.0 - MathUtil.smoothstep(0, 1, precipitationDistance);

			planetPoint.plants.add(Tuple2.of(plantData, plant));
			
			planetPoint.plantColor = planetPoint.plantColor.interpolate(plantData.color, plant);
			planetPoint.color = planetPoint.color.interpolate(plantData.color, plant);
			
			planetPoint.temperature += plant * plantData.temperatureInfluence;
		}
	}
	
	private static double deviationDistance(double value, double optimum, double minusDeviation, double plusDeviation) {
		double distance = value - optimum;
		distance /= distance > 0 ? plusDeviation : minusDeviation;
		distance = Math.abs(distance);
		return distance;
	}

	public static class PlantData {
		public final String name;
		public final double precipitationOptimum;
		public final double precipitationMinusDeviation;
		public final double precipitationPlusDeviation;
		public final double temperatureOptimum;
		public final double temperatureMinusDeviation;
		public final double temperaturePlusDeviation;
		public final double temperatureInfluence;
		public final Color color;

		private PlantData(String name, double precipitationOptimum, double precipitationMinusDeviation, double precipitationPlusDeviation, double temperatureOptimum, double temperatureMinusDeviation, double temperaturePlusDeviation, double temperatureInfluence, Color color) {
			this.name = name;
			this.precipitationOptimum = precipitationOptimum;
			this.precipitationMinusDeviation = precipitationMinusDeviation;
			this.precipitationPlusDeviation = precipitationPlusDeviation;
			this.temperatureOptimum = temperatureOptimum;
			this.temperatureMinusDeviation = temperatureMinusDeviation;
			this.temperaturePlusDeviation = temperaturePlusDeviation;
			this.temperatureInfluence = temperatureInfluence;
			this.color = color;
		}
		
		public static PlantData of(String name, double precipitationOptimum, double precipitationMinusDeviation, double precipitationPlusDeviation, double temperatureOptimum, double temperatureMinusDeviation, double temperaturePlusDeviation, double temperatureInfluence, Color color) {
			return new PlantData(name, precipitationOptimum, precipitationMinusDeviation, precipitationPlusDeviation, temperatureOptimum, temperatureMinusDeviation, temperaturePlusDeviation, temperatureInfluence, color);
		}
	}
}
