package ch.obermuhlner.planetgen.planet.layer;

import java.util.List;

import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

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
			double temperatureDistance = Math.abs(planetPoint.temperature - plantData.temperatureOptimum) / plantData.temperatureDeviation;
			double precipitationDistance = Math.abs(planetPoint.precipitation - plantData.precipitationOptimum) / plantData.precipitationDeviation;
			double plant = 1.0;
			plant *= 1.0 - MathUtil.smoothstep(0, 1, temperatureDistance);
			plant *= 1.0 - MathUtil.smoothstep(0, 1, precipitationDistance);

			planetPoint.plantColor = planetPoint.plantColor.interpolate(plantData.color, plant);
			planetPoint.color = planetPoint.color.interpolate(plantData.color, plant);
			
			planetPoint.temperature += plant * plantData.temperatureInfluence;
		}
	}

	public static class PlantData {
		public final String name;
		public final double precipitationOptimum;
		public final double precipitationDeviation;
		public final double temperatureOptimum;
		public final double temperatureDeviation;
		public final double temperatureInfluence;
		public final Color color;

		private PlantData(String name, double precipitationOptimum, double precipitationDeviation, double temperatureOptimum, double temperatureDeviation, double temperatureInfluence, Color color) {
			this.name = name;
			this.precipitationOptimum = precipitationOptimum;
			this.precipitationDeviation = precipitationDeviation;
			this.temperatureOptimum = temperatureOptimum;
			this.temperatureDeviation = temperatureDeviation;
			this.temperatureInfluence = temperatureInfluence;
			this.color = color;
		}
		
		public static PlantData of(String name, double precipitationOptimum, double precipitationDeviation, double temperatureOptimum, double temperatureDeviation, double temperatureInfluence, Color color) {
			return new PlantData(name, precipitationOptimum, precipitationDeviation, temperatureOptimum, temperatureDeviation, temperatureInfluence, color);
		}
	}
}
