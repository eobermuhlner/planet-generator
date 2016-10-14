package ch.obermuhlner.planetgen.planet.layer;

import java.util.Random;
import java.util.function.Function;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;

public class CraterLayer implements Layer {

	private CraterFunction craterFunction = new CraterFunction(
			craterPart(0.0, 1.0, d -> (d * d) * 4 - 3),
			craterPart(0.9, 1.5, d -> 1.0 - MathUtil.smoothstep(0, 1, d)));
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		Random random = new Random(1);
		
		CraterCalculator craterCalculators[] = {
//				new HeightCraterCalculator(2000, new FixPolarCraterCalculator()),
//				new HeightCraterCalculator(2000, new FixCartesianCraterCalculator()),
//				new HeightCraterCalculator(2000, new GridPolarCraterCalculator()),

				new HeightCraterCalculator(2000, new GridCartesianCraterCalculator(random, 2, 2000000)),
				new HeightCraterCalculator(1000, new GridCartesianCraterCalculator(random, 4, 2000000))
		};
		
		for (CraterCalculator craterCalculator : craterCalculators) {
			planetPoint.groundHeight += craterCalculator.calculateCraters(latitude, longitude, planet.planetData.radius, craterFunction);
		}
		planetPoint.height = planetPoint.groundHeight;
	}

	public static interface CraterCalculator {
		double calculateCraters(double latitude, double longitude, double planetRadius, CraterFunction craterFunction);
	}

	public static class HeightCraterCalculator implements CraterCalculator {
		private double height;
		private CraterCalculator craterCalculator;

		public HeightCraterCalculator(double height, CraterCalculator craterCalculator) {
			this.height = height;
			this.craterCalculator = craterCalculator;
		}

		@Override
		public double calculateCraters(double latitude, double longitude, double planetRadius, CraterFunction craterFunction) {
			return craterCalculator.calculateCraters(latitude, longitude, planetRadius, craterFunction) * height;
		}
	}
	
	public static class FixPolarCraterCalculator implements CraterCalculator {
		private double craterLatitude = Planet.EQUATOR_LATITUDE / 4;
		private double craterLongitude = Planet.CENTER_LONGITUDE;
		private double craterRadius = 0.2;
		
		@Override
		public double calculateCraters(double latitude, double longitude, double planetRadius, CraterFunction craterFunction) {
			double distance = length(craterLatitude - latitude, craterLongitude - longitude);
			
			return craterFunction.calculate(distance / craterRadius);
		}
	}
	
	public static class GridPolarCraterCalculator implements CraterCalculator {
		private double grid = 4;
		private double craterRadius = 0.5;
		
		@Override
		public double calculateCraters(double latitude, double longitude, double planetRadius, CraterFunction craterFunction) {
			Vector2 point = Vector2.of(latitude, longitude);
			Vector2 big = point.multiply(grid);
			Vector2 fract = big.subtract(big.floor());
			Vector2 dist = fract.subtract(0.5);

			double distance = dist.getLength();

			return craterFunction.calculate(distance / craterRadius);
		}
	}
	
	public static class FixCartesianCraterCalculator implements CraterCalculator {
		private double craterLatitude = Planet.EQUATOR_LATITUDE / 4;
		private double craterLongitude = Planet.CENTER_LONGITUDE;
		private double craterRadius = 500000;
		
		@Override
		public double calculateCraters(double latitude, double longitude, double planetRadius, CraterFunction craterFunction) {
			Vector3 pointCartesian = Vector3.ofPolar(latitude, longitude, planetRadius);
			Vector3 craterCartesian = Vector3.ofPolar(craterLatitude, craterLongitude, planetRadius);
			double distance = pointCartesian.subtract(craterCartesian).getLength();

			return craterFunction.calculate(distance / craterRadius);
		}
	}

	public static class GridCartesianCraterCalculator implements CraterCalculator {
		private Random random;
		private double grid;
		private double craterRadius;
		
		public GridCartesianCraterCalculator(Random random, double grid, double craterRadius) {
			this.random = random;
			this.grid = grid;
			this.craterRadius = craterRadius;
		}

		@Override
		public double calculateCraters(double latitude, double longitude, double planetRadius, CraterFunction craterFunction) {
			Vector2 point = Vector2.of(
					latitude / Planet.RANGE_LATITUDE,
					longitude / Planet.RANGE_LONGITUDE);
			Vector2 big = point.multiply(grid);
			Vector2 fract = big.subtract(big.floor());
			Vector2 dist = fract.subtract(0.5);
//			Vector2 randomDisplacement = Vector2.of(
//					random.nextDouble() * 0.2 - 0.1,
//					random.nextDouble() * 0.2 - 0.1);
//			dist = dist.add(randomDisplacement);
			Vector2 craterPoint = point.subtract(dist);
			craterPoint = Vector2.of(
					MathUtil.clamp(craterPoint.x, 0.0, 1.0) * Planet.RANGE_LATITUDE,
					MathUtil.wrap(craterPoint.y, 1.0) * Planet.RANGE_LONGITUDE);

			Vector3 pointCartesian = Vector3.ofPolar(latitude, longitude, planetRadius);
			Vector3 craterCartesian = Vector3.ofPolar(craterPoint.x, craterPoint.y, planetRadius);
			double distance = pointCartesian.subtract(craterCartesian).getLength();
			
			return craterFunction.calculate(distance / craterRadius);
		}
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
	
	public static class CraterPartFunction {
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
