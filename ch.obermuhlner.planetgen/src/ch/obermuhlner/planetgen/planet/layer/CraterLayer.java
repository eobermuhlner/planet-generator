package ch.obermuhlner.planetgen.planet.layer;

import java.util.function.Function;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.util.Random;

public class CraterLayer implements Layer {

	public static CraterFunction simpleRoundCraterFunction = new CraterFunction(
			craterPart(0.0, 0.7, d -> (d * d) * 4 - 3),
			craterPart(0.6, 1.0, d -> 1.0 - MathUtil.smoothstep(0, 1, d)));

	public static CraterFunction simpleFlatCraterFunction = new CraterFunction(
			craterPart(0.0, 0.7, d -> -2.2),
			craterPart(0.3, 1.0, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.8));

	public static CraterFunction complexFlatCraterFunction = new CraterFunction(
			craterPart(0.0, 0.1, d -> -0.2),
			craterPart(0.0, 0.7, d -> -1.5),
			craterPart(0.4, 1.0, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.6));

	public static CraterFunction complexStepsCraterFunction = new CraterFunction(
			craterPart(0.00, 0.10, d -> -0.3),
			craterPart(0.00, 0.53, d -> -0.8),
			craterPart(0.50, 0.63, d -> -0.4),
			craterPart(0.60, 0.73, d -> 0.0),
			craterPart(0.7, 1.0, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.4));
	
	private CraterCalculator[] craterCalculators;
	
	public CraterLayer() {
		double baseHeight = 5000;

		craterCalculators = new CraterCalculator[] {
				createCraterCalculator(baseHeight,  5, complexStepsCraterFunction),
				createCraterCalculator(baseHeight,  7, complexStepsCraterFunction),
				createCraterCalculator(baseHeight,  11, complexFlatCraterFunction),
				createCraterCalculator(baseHeight,  17, complexFlatCraterFunction),
				createCraterCalculator(baseHeight,  23, simpleFlatCraterFunction),
				createCraterCalculator(baseHeight,  31, simpleFlatCraterFunction),
				createCraterCalculator(baseHeight,  47, simpleFlatCraterFunction),
				createCraterCalculator(baseHeight,  79, simpleRoundCraterFunction),
				createCraterCalculator(baseHeight, 113, simpleRoundCraterFunction),
				createCraterCalculator(baseHeight, 201, simpleRoundCraterFunction),
				createCraterCalculator(baseHeight, 471, simpleRoundCraterFunction),
				createCraterCalculator(baseHeight, 693, simpleRoundCraterFunction),
				createCraterCalculator(baseHeight, 877, simpleRoundCraterFunction),
		};
	}

	private CraterCalculator createCraterCalculator(double baseHeight, double grid, CraterFunction craterFunction) {
		return new HeightCraterCalculator(baseHeight / grid, new GridCartesianCraterCalculator( grid, craterFunction));
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		long[] seed = new long[planet.planetData.seed.length + 2]; // add 2 empty entries in seed - will be filled later with crater coords
		System.arraycopy(planet.planetData.seed, 0, seed, 0, planet.planetData.seed.length);
		
		for (CraterCalculator craterCalculator : craterCalculators) {
			planetPoint.groundHeight += craterCalculator.calculateCraters(latitude, longitude, seed, planet.planetData.radius, context.accuracy);
		}
		
		planetPoint.height = planetPoint.groundHeight;
	}

	public static interface CraterCalculator {
		double calculateCraters(double latitude, double longitude, long[] seed, double planetRadius, double accuracy);
	}

	public static class HeightCraterCalculator implements CraterCalculator {
		private double height;
		private CraterCalculator craterCalculator;

		public HeightCraterCalculator(double height, CraterCalculator craterCalculator) {
			this.height = height;
			this.craterCalculator = craterCalculator;
		}

		@Override
		public double calculateCraters(double latitude, double longitude, long[] seed, double planetRadius, double accuracy) {
			if (height < accuracy) {
				return 0;
			}
			return craterCalculator.calculateCraters(latitude, longitude, seed, planetRadius, accuracy) * height;
		}
	}
	
	public static class GridCartesianCraterCalculator implements CraterCalculator {
		private double grid;
		private CraterFunction craterFunction;
		
		public GridCartesianCraterCalculator(double grid, CraterFunction craterFunction) {
			this.grid = grid;
			this.craterFunction = craterFunction;
		}

		@Override
		public double calculateCraters(double latitude, double longitude, long[] seed, double planetRadius, double accuracy) {
			Vector2 point = Vector2.of(
					latitude / Planet.RANGE_LATITUDE,
					longitude / Planet.RANGE_LONGITUDE);
			Vector2 big = point.multiply(grid);
			Vector2 bigFloor = big.floor();

			seed[seed.length - 2] = (long)bigFloor.x;
			seed[seed.length - 1] = (long)bigFloor.y;
			Random random = new Random(seed);
			
			Vector2 grid1Point = normalizedToPolar(bigFloor.divide(grid));
			Vector2 grid2Point = normalizedToPolar(bigFloor.add(Vector2.of(0, 1)).divide(grid));
			Vector2 grid3Point = normalizedToPolar(bigFloor.add(Vector2.of(1, 0)).divide(grid));
			Vector2 grid4Point = normalizedToPolar(bigFloor.add(Vector2.of(1, 1)).divide(grid));
			double randomSize = random.nextDouble(0.1, 0.5);
			Vector2 randomDisplacement = Vector2.of(
				random.nextDouble(randomSize / 2, 1.0 - randomSize / 2),
				random.nextDouble(randomSize / 2, 1.0 - randomSize / 2));
			Vector2 craterPoint = normalizedToPolar(bigFloor.add(randomDisplacement).divide(grid));

			Vector3 pointCartesian = Vector3.ofPolar(latitude, longitude, planetRadius);
			Vector3 craterCartesian = Vector3.ofPolar(craterPoint.x, craterPoint.y, planetRadius);
			double distance = pointCartesian.subtract(craterCartesian).getLength();

			Vector3 grid1Cartesian = Vector3.ofPolar(grid1Point.x, grid1Point.y, planetRadius);
			Vector3 grid2Cartesian = Vector3.ofPolar(grid2Point.x, grid2Point.y, planetRadius);
			Vector3 grid3Cartesian = Vector3.ofPolar(grid3Point.x, grid3Point.y, planetRadius);
			Vector3 grid4Cartesian = Vector3.ofPolar(grid4Point.x, grid4Point.y, planetRadius);
			double gridSize1 = grid1Cartesian.subtract(grid2Cartesian).getLength();
			double gridSize2 = grid1Cartesian.subtract(grid3Cartesian).getLength();
			double gridSize3 = grid3Cartesian.subtract(grid4Cartesian).getLength();
			double gridSize = Math.min(gridSize1, Math.min(gridSize2, gridSize3));
			gridSize *= randomSize;
			if (gridSize == 0) {
				return 0;
			}
			
			double relativeDistance = distance / gridSize;
			if (relativeDistance > 1.0) {
				return 0;
			}
			
			return craterFunction.calculate(relativeDistance);
		}

		private Vector2 normalizedToPolar(Vector2 normalized) {
			return Vector2.of(normalized.x * Planet.RANGE_LATITUDE, normalized.y * Planet.RANGE_LONGITUDE);
		}
	}
	
	public static class CraterFunction {
		private final CraterPartFunction[] craterPartFunctions;

		public CraterFunction(CraterPartFunction... craterPartFunctions) {
			this.craterPartFunctions = craterPartFunctions;
		}
		
		public double calculate(double distance) {
			distance = Math.abs(distance);
			
			double totalResult = 0;
			
			double lastMaxDist = 0;
			double lastResult = 0;
			
			for (CraterPartFunction craterPartFunction : craterPartFunctions) {
				if (distance >= craterPartFunction.minDist && distance <= craterPartFunction.maxDist) {
					double correctedDistance = (distance - craterPartFunction.minDist) / (craterPartFunction.maxDist - craterPartFunction.minDist);
					double result = craterPartFunction.function.apply(correctedDistance);
					
					if (craterPartFunction.minDist < lastMaxDist && distance < lastMaxDist) {
						double weight = MathUtil.smoothstep(0, 1, (distance  - craterPartFunction.minDist) / (lastMaxDist - craterPartFunction.minDist));
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
}
