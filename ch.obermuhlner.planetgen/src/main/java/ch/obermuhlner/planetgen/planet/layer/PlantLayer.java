package ch.obermuhlner.planetgen.planet.layer;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.util.Tuple2;

public class PlantLayer implements Layer {

	private static final Color PLANT_SPECULAR_COLOR = Color.rgb(0.2, 0.2, 0.2);

	private List<PlantData> plantDatas;

	public PlantLayer(List<PlantData> plantDatas) {
		this.plantDatas = plantDatas;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		if (planetPoint.isWater || planetPoint.iceHeight > 0) {
			planetPoint.plantColor = null;
			return;
		}

		if (!plantDatas.isEmpty()) {
			planetPoint.plantColor = Color.BLACK;
		}
		
		planetPoint.plants = new ArrayList<>();
		for (PlantData plantData : plantDatas) {
			double plant = plantData.plantGrowth(planetPoint.temperatureAverage, planetPoint.precipitationAverage);
			
			planetPoint.plants.add(Tuple2.of(plantData, plant));
			
			planetPoint.plantColor = planetPoint.plantColor.interpolate(plantData.color, plant);
			planetPoint.color = planetPoint.color.interpolate(plantData.color, plant);
			planetPoint.specularColor = planetPoint.specularColor.interpolate(PLANT_SPECULAR_COLOR, plant);

			planetPoint.temperature += plant * plantData.temperatureInfluence;
		}
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
		
		public double plantGrowth(double temperatureAverage, double precipitationAverage) {
			double temperatureDistance = MathUtil.deviationDistance(temperatureAverage, temperatureOptimum, temperatureMinusDeviation, temperaturePlusDeviation);
			double precipitationDistance = MathUtil.deviationDistance(precipitationAverage, precipitationOptimum, precipitationMinusDeviation, precipitationPlusDeviation); 

			double plant = 1.0;
			plant *= 1.0 - MathUtil.smoothstep(0, 1, temperatureDistance);
			plant *= 1.0 - MathUtil.smoothstep(0, 1, precipitationDistance);
			
			return plant;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public static PlantData of(String name, double precipitationOptimum, double precipitationMinusDeviation, double precipitationPlusDeviation, double temperatureOptimum, double temperatureMinusDeviation, double temperaturePlusDeviation, double temperatureInfluence, Color color) {
			return new PlantData(name, precipitationOptimum, precipitationMinusDeviation, precipitationPlusDeviation, temperatureOptimum, temperatureMinusDeviation, temperaturePlusDeviation, temperatureInfluence, color);
		}
	}
}
