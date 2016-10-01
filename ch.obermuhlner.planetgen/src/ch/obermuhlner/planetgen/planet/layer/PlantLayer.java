package ch.obermuhlner.planetgen.planet.layer;

import java.util.List;

import ch.obermuhlner.planetgen.height.NoiseHeight;
import ch.obermuhlner.planetgen.math.Color;
import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public class PlantLayer implements Layer {

	private List<PlantData> plantDatas;
	private NoiseHeight noiseHeight;

	public PlantLayer(List<PlantData> plantDatas, NoiseHeight noiseHeight) {
		this.plantDatas = plantDatas;
		this.noiseHeight = noiseHeight;
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context) {
		if (planetPoint.isWater || planetPoint.iceHeight > 0) {
			planetPoint.plantColor = null;
			return;
		}

		double noise = noiseHeight.height(latitude, longitude, context);

		if (!plantDatas.isEmpty()) {
			planetPoint.plantColor = Color.BLACK;
		}
		
		for (PlantData plantData : plantDatas) {
			double distance = Math.abs(planetData.baseTemperature - plantData.temperatureOptimum) / plantData.temperatureDeviation;
			double plant = 1.0 - MathUtil.smoothstep(0, 1, distance);
			plant *= noise;

			planetPoint.plantColor = planetPoint.plantColor.interpolate(plantData.color, plant);
			planetPoint.color = planetPoint.color.interpolate(plantData.color, plant);
			
			planetPoint.temperature += plant * plantData.temperatureInfluence;
		}
	}

	public static class PlantData {
		public final double temperatureOptimum;
		public final double temperatureDeviation;
		public final double temperatureInfluence;
		public final Color color;

		private PlantData(double temperatureOptimum, double temperatureDeviation, double temperatureInfluence, Color color) {
			this.temperatureOptimum = temperatureOptimum;
			this.temperatureDeviation = temperatureDeviation;
			this.temperatureInfluence = temperatureInfluence;
			this.color = color;
		}
		
		public static PlantData of(double temperatureOptimum, double temperatureDeviation, double temperatureInfluence, Color color) {
			return new PlantData(temperatureOptimum, temperatureDeviation, temperatureInfluence, color);
		}

	}
}
