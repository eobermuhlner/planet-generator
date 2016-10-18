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
		int baseGrid = 4;
		double baseHeight = 1000;

		craterCalculators = new CraterCalculator[] {
			new HeightCraterCalculator(baseHeight /   1, new GridCartesianCraterCalculator(baseGrid *   1, complexStepsCraterFunction)),
			new HeightCraterCalculator(baseHeight /   2, new GridCartesianCraterCalculator(baseGrid *   2, complexStepsCraterFunction)),
			new HeightCraterCalculator(baseHeight /   3, new GridCartesianCraterCalculator(baseGrid *   3, complexFlatCraterFunction)),
			new HeightCraterCalculator(baseHeight /   4, new GridCartesianCraterCalculator(baseGrid *   4, complexFlatCraterFunction)),
			new HeightCraterCalculator(baseHeight /   5, new GridCartesianCraterCalculator(baseGrid *   5, complexFlatCraterFunction)),
			new HeightCraterCalculator(baseHeight /   6, new GridCartesianCraterCalculator(baseGrid *   6, complexFlatCraterFunction)),
			new HeightCraterCalculator(baseHeight /   8, new GridCartesianCraterCalculator(baseGrid *   8, simpleFlatCraterFunction)),
			new HeightCraterCalculator(baseHeight /  10, new GridCartesianCraterCalculator(baseGrid *  10, simpleFlatCraterFunction)),
			new HeightCraterCalculator(baseHeight /  14, new GridCartesianCraterCalculator(baseGrid *  14, simpleFlatCraterFunction)),
			new HeightCraterCalculator(baseHeight /  20, new GridCartesianCraterCalculator(baseGrid *  20, simpleRoundCraterFunction)),
			new HeightCraterCalculator(baseHeight /  40, new GridCartesianCraterCalculator(baseGrid *  40, simpleRoundCraterFunction)),
			new HeightCraterCalculator(baseHeight /  60, new GridCartesianCraterCalculator(baseGrid *  60, simpleRoundCraterFunction)),
			new HeightCraterCalculator(baseHeight / 100, new GridCartesianCraterCalculator(baseGrid * 100, simpleRoundCraterFunction)),
			new HeightCraterCalculator(baseHeight / 200, new GridCartesianCraterCalculator(baseGrid * 200, simpleRoundCraterFunction)),
			new HeightCraterCalculator(baseHeight / 300, new GridCartesianCraterCalculator(baseGrid * 300, simpleRoundCraterFunction)),
			new HeightCraterCalculator(baseHeight / 400, new GridCartesianCraterCalculator(baseGrid * 400, simpleRoundCraterFunction)),
			new HeightCraterCalculator(baseHeight / 500, new GridCartesianCraterCalculator(baseGrid * 500, simpleRoundCraterFunction)),
			new HeightCraterCalculator(baseHeight / 800, new GridCartesianCraterCalculator(baseGrid * 800, simpleRoundCraterFunction)),
		};
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
			
			Vector2 fract = big.subtract(bigFloor);
			Vector2 dist = fract.subtract(0.5);
			Vector2 randomDisplacement = Vector2.of(
					random.nextDouble(-0.2, 0.2),
					random.nextDouble(-0.2, 0.2));
			dist = dist.add(randomDisplacement);
			Vector2 craterPoint = point.subtract(dist);
			craterPoint = Vector2.of(
					MathUtil.clamp(craterPoint.x, 0.0, 1.0) * Planet.RANGE_LATITUDE,
					MathUtil.wrap(craterPoint.y, 1.0) * Planet.RANGE_LONGITUDE);

			Vector3 pointCartesian = Vector3.ofPolar(latitude, longitude, planetRadius);
			Vector3 craterCartesian = Vector3.ofPolar(craterPoint.x, craterPoint.y, planetRadius);
			double distance = pointCartesian.subtract(craterCartesian).getLength();
			
			double latitudeCircumference = Math.cos(bigFloor.x / grid * Planet.RANGE_LATITUDE - Planet.EQUATOR_LATITUDE) * planetRadius * 2 * Math.PI;
			double randomCraterRadius = latitudeCircumference / grid / 2;
			randomCraterRadius = randomCraterRadius * random.nextDouble(0.5, 1.0);
			double relativeDistance = distance / randomCraterRadius;
			if (relativeDistance > 1.0) {
				return 0;
			}
			
			return craterFunction.calculate(relativeDistance);
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
