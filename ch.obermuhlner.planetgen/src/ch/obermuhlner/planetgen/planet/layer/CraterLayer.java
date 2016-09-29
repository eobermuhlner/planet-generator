package ch.obermuhlner.planetgen.planet.layer;

import java.util.function.Function;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public class CraterLayer implements Layer {

	private CraterFunction craterFunction = new CraterFunction(
			craterPart(0.0, 1.0, d -> (d * d) * 4 - 3),
			craterPart(0.9, 1.5, d -> 1.0 - MathUtil.smoothstep(0, 1, d)));
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, PlanetData planetData, double latitude, double longitude, PlanetGenerationContext context) {

		double craterHeight = crater(latitude, longitude, 4);
		planetPoint.groundHeight += craterHeight;
		planetPoint.height = planetPoint.groundHeight;
	}

	private double crater(double latitude, double longitude, double grid) {
		double latitudeSector = latitude * grid;
		double longitudeSector = longitude * grid;
		
		double latitudeSectorFloor = Math.floor(latitudeSector);
		double longitudeSectorFloor = Math.floor(longitudeSector);

		double latitudeCrater = latitudeSector - latitudeSectorFloor - 0.5;
		double longitudeCrater = longitudeSector - longitudeSectorFloor - 0.5;
		
		double radius = 0.2;
		double distance = length(latitudeCrater, longitudeCrater);

		return calculate(radius, distance) * 1000;
	}
	
	private double calculate(double radius, double distance) {
		double relativeDistance = distance / radius;
		
		if (relativeDistance > 2.0) {
			return 0;
		}
				
		return craterFunction.calculate(relativeDistance);
	}
	
	public static class CraterFunction {
		private final CraterPartFunction[] craterPartFunctions;

		public CraterFunction(CraterPartFunction... craterPartFunctions) {
			this.craterPartFunctions = craterPartFunctions;
		}
		
		public double calculate(double distance) {
			double totalResult = 0;
			
			double lastMaxDist = 0;
			double lastResult = 0;
			
			for (CraterPartFunction craterPartFunction : craterPartFunctions) {
				if (distance >= craterPartFunction.minDist && distance <= craterPartFunction.maxDist) {
					double correctedDistance = (distance - craterPartFunction.minDist) / (craterPartFunction.maxDist - craterPartFunction.minDist);
					double result = craterPartFunction.function.apply(correctedDistance);
					
					if (craterPartFunction.minDist < lastMaxDist && distance < lastMaxDist) {
						double weight = (distance  - craterPartFunction.minDist) / (lastMaxDist - craterPartFunction.minDist);
						result = MathUtil.mix(lastResult, result, weight);
						totalResult -= lastResult;
						totalResult += result;
					} else {
						totalResult += result;
					}
					
					lastMaxDist = craterPartFunction.maxDist;
					lastResult = result;
				}
			}
			
			return totalResult;
		}
	}
	
	private static class CraterPartFunction {
		public final double minDist;
		public final double maxDist;
		public final Function<Double, Double> function;
		
		public CraterPartFunction(double minDist, double maxDist, Function<Double, Double> function) {
			this.minDist = minDist;
			this.maxDist = maxDist;
			this.function = function;
		}
	}
	
	private static CraterPartFunction craterPart(double minDist, double maxDist, Function<Double, Double> func) {
		return new CraterPartFunction(minDist, maxDist, func);
	}

	private static double length(double x, double y) {
		return Math.sqrt(x*x + y*y);
	}
}
