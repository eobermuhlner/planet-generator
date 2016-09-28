package ch.obermuhlner.planetgen.planet.layer;

import java.util.function.Function;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;

public class CraterLayer implements Layer {

	@Override
	public void calculateLayerState(LayerState layerState, PlanetData planetData, double latitude, double longitude, double accuracy) {

		double craterHeight = crater(latitude, longitude, 4);
		layerState.height += craterHeight;
	}

	private double crater(double latitude, double longitude, double grid) {
//		double latitudeSector = Planet.RANGE_LATITUDE * grid;
//		double longitudeSector = Planet.RANGE_LONGITUDE * grid;
//		
//		double latitudeSectorFloor = Math.floor(latitudeSector);
//		double longitudeSectorFloor = Math.floor(longitudeSector);
//
//		double latitudeSectorFract = latitudeSector - latitudeSectorFloor;
//		double longitudeSectorFract = longitudeSector - longitudeSectorFloor;
//		
//		double radius = 0.3;
//		double distance = length(latitudeSectorFract - 0.5, longitudeSectorFract - 0.5);
//
//		return craterFunction(radius, distance) * 1000;
		
		double latitudeCrater = Planet.EQUATOR_LATITUDE;
		double longitudeCrater = 1.0;
		
		double latitudeDistance = (latitude-latitudeCrater) / Planet.RANGE_LATITUDE / 2;
		double longitudeDistance = (longitude-longitudeCrater) / Planet.RANGE_LONGITUDE;
		double distance = length(latitudeDistance, longitudeDistance);
		return craterFunction(0.02, distance) * 5000;
	}
	
	private static double craterFunction(double radius, double distance) {
		double relativeDistance = distance / radius;
		
		if (relativeDistance > 2.0) {
			return 0;
		}
		
		return calculate(relativeDistance,
			func(0.0, 1.0, d -> (d * d) * 4 - 3),
			func(0.9, 1.5, d -> 1.0 - MathUtil.smoothstep(0, 1, d)));
	}
	
	private static double calculate(double distance, CraterFunction... craterFunctions) {
		double totalResult = 0;
		
		double lastMaxDist = 0;
		double lastResult = 0;
		
		for (CraterFunction craterFunction : craterFunctions) {
			if (distance >= craterFunction.minDist && distance <= craterFunction.maxDist) {
				double correctedDistance = (distance - craterFunction.minDist) / (craterFunction.maxDist - craterFunction.minDist);
				double result = craterFunction.function.apply(correctedDistance);
				
				if (craterFunction.minDist < lastMaxDist && distance < lastMaxDist) {
					double weight = (distance  - craterFunction.minDist) / (lastMaxDist - craterFunction.minDist);
					result = MathUtil.mix(lastResult, result, weight);
					totalResult -= lastResult;
					totalResult += result;
				} else {
					totalResult += result;
				}
				
				lastMaxDist = craterFunction.maxDist;
				lastResult = result;
			}
		}
		
		return totalResult;
	}
	
	private static class CraterFunction {
		public final double minDist;
		public final double maxDist;
		public final Function<Double, Double> function;
		
		public CraterFunction(double minDist, double maxDist, Function<Double, Double> function) {
			this.minDist = minDist;
			this.maxDist = maxDist;
			this.function = function;
		}
	}
	
	private static CraterFunction func(double minDist, double maxDist, Function<Double, Double> func) {
		return new CraterFunction(minDist, maxDist, func);
	}

	private static double length(double x, double y) {
		return Math.sqrt(x*x + y*y);
	}

	public static void main(String[] args) {
		for(double d = 0; d < 2; d += 0.01) {
			System.out.printf("%8.5f; %8.5f\n", d, craterFunction(1.0, d));
		}
	}
}
