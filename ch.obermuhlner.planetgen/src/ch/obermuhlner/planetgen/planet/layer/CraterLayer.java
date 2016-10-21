package ch.obermuhlner.planetgen.planet.layer;

import java.util.function.Function;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.NoisePolarValue;
import ch.obermuhlner.planetgen.value.NoiseSphereValue;
import ch.obermuhlner.util.Random;

public class CraterLayer implements Layer {

	public static Crater simpleRoundCrater = new Crater(
			"Simple Round Crater",
			new CraterFunction(
					craterPart(0.0, 0.7, d -> (d * d) * 4 - 3),
					craterPart(0.6, 1.0, d -> 1.0 - MathUtil.smoothstep(0, 1, d))),
			new CraterFunction(
					craterPart(0.0, 0.7, d -> 0.0),
					craterPart(0.5, 1.0, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.2)),
			new CraterFunction(
					craterPart(0.0, 1.0, d -> 0.0)));
	
	public static Crater simpleFlatCrater = new Crater(
			"Simple Flat Crater",
			new CraterFunction(
					craterPart(0.0, 0.6, d -> -2.2),
					craterPart(0.3, 1.0, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.8)),
			new CraterFunction(
					craterPart(0.0, 0.7, d -> 0.0),
					craterPart(0.5, 1.0, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.2)),
			new CraterFunction(
					craterPart(0.0, 1.0, d -> 0.0)));
	
	public static Crater complexFlatCrater = new Crater(
			"Complex Flat Crater",
			new CraterFunction(
					craterPart(0.0, 0.1, d -> -0.2),
					craterPart(0.0, 0.6, d -> -1.5),
					craterPart(0.4, 1.0, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.6)),
			new CraterFunction(
					craterPart(0.0, 0.1, d -> 0.6),
					craterPart(0.0, 0.7, d -> 0.0),
					craterPart(0.5, 1.0, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.2)),
			new CraterFunction(
					craterPart(0.0, 0.7, d -> 0.0),
					craterPart(0.4, 0.9, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.1)));
	
	public static Crater complexStepsCrater = new Crater(
			"Complex Steps Crater",
			new CraterFunction(
					craterPart(0.00, 0.10, d -> -0.3),
					craterPart(0.00, 0.53, d -> -0.8),
					craterPart(0.50, 0.63, d -> -0.4),
					craterPart(0.60, 0.73, d -> 0.0),
					craterPart(0.7, 1.0, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.4)),
			new CraterFunction(
					craterPart(0.0, 0.1, d -> 0.8),
					craterPart(0.0, 0.7, d -> 0.0),
					craterPart(0.5, 1.0, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.2)),
			new CraterFunction(
					craterPart(0.0, 0.7, d -> 0.0),
					craterPart(0.4, 0.9, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.2)));

	public static Crater simpleVolcano = new Crater(
			"Simple Volcano",
			new CraterFunction(
					craterPart(0.0, 0.2, d -> 1.0),
					craterPart(0.1, 0.6, d -> 1.0 - MathUtil.smoothstep(0, 1, d))),
			new CraterFunction(
					craterPart(0.0, 0.2, d -> 0.1),
					craterPart(0.2, 0.3, d -> (1.0 - MathUtil.smoothstep(0, 1, d)) * 0.1)),
			new CraterFunction(
					craterPart(0.0, 0.9, d -> 0.2),
					craterPart(0.3, 1.0, d -> 0.0)));

	private final NoiseSphereValue heightNoiseValue;
	private final NoisePolarValue radialNoiseValue;
	private final CraterCalculator[] craterCalculators;

	public CraterLayer(NoiseSphereValue heightNoiseValue, NoisePolarValue radialNoiseValue) {
		this.heightNoiseValue = heightNoiseValue;
		this.radialNoiseValue = radialNoiseValue;
		
		double baseHeight = 2000;

		craterCalculators = new CraterCalculator[] {
				createCraterCalculator(baseHeight,   13, simpleVolcano),

				createCraterCalculator(baseHeight,   5, complexStepsCrater),
				createCraterCalculator(baseHeight,   7, complexStepsCrater),
				createCraterCalculator(baseHeight,   11, complexFlatCrater),
				createCraterCalculator(baseHeight,   17, complexFlatCrater),
				createCraterCalculator(baseHeight,   23, complexFlatCrater),
				createCraterCalculator(baseHeight,   31, simpleFlatCrater),
				createCraterCalculator(baseHeight,   51, simpleFlatCrater),
				createCraterCalculator(baseHeight,   79, simpleFlatCrater),
				createCraterCalculator(baseHeight,  113, simpleRoundCrater),
				createCraterCalculator(baseHeight,  201, simpleRoundCrater),
				createCraterCalculator(baseHeight,  471, simpleRoundCrater),
				createCraterCalculator(baseHeight,  693, simpleRoundCrater),
				createCraterCalculator(baseHeight,  877, simpleRoundCrater),
				createCraterCalculator(baseHeight, 1003, simpleRoundCrater),
				createCraterCalculator(baseHeight, 1301, simpleRoundCrater),
				createCraterCalculator(baseHeight, 1707, simpleRoundCrater),
		};
	}

	private CraterCalculator createCraterCalculator(double baseHeight, int grid, Crater crater) {
		return new HeightCraterCalculator(baseHeight / grid, new GridCartesianCraterCalculator( grid, crater));
	}
	
	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		if (planet.planetData.craterDensity <= 0.0) {
			return;
		}
		
		long[] seed = new long[planet.planetData.seed.length + 2]; // add 2 empty entries in seed - will be filled later with crater coords
		System.arraycopy(planet.planetData.seed, 0, seed, 0, planet.planetData.seed.length);
		
		for (CraterCalculator craterCalculator : craterCalculators) {
			planetPoint.groundHeight += craterCalculator.calculateCraters(latitude, longitude, seed, planet.planetData, context);
		}
		
		planetPoint.height = planetPoint.groundHeight;
	}

	public static interface CraterCalculator {
		double calculateCraters(double latitude, double longitude, long[] seed, PlanetData planetData, PlanetGenerationContext context);
	}

	public static class HeightCraterCalculator implements CraterCalculator {
		private double height;
		private CraterCalculator craterCalculator;

		public HeightCraterCalculator(double height, CraterCalculator craterCalculator) {
			this.height = height;
			this.craterCalculator = craterCalculator;
		}

		@Override
		public double calculateCraters(double latitude, double longitude, long[] seed, PlanetData planetData, PlanetGenerationContext context) {
			if (height < context.accuracy) {
				return 0;
			}
			return craterCalculator.calculateCraters(latitude, longitude, seed, planetData, context) * height;
		}
	}
	
	public class GridCartesianCraterCalculator implements CraterCalculator {
		private double grid;
		private Crater crater;
		private double[] gridSizes;
		
		public GridCartesianCraterCalculator(int grid, Crater crater) {
			this.grid = grid;
			this.crater = crater;
			
			gridSizes = new double[grid];
			for (int i = 0; i < grid; i++) {
				Vector2 grid1Point = normalizedToPolar(Vector2.of(i, i).divide(grid));
				Vector2 grid2Point = normalizedToPolar(Vector2.of(i, i+1).divide(grid));
				Vector2 grid3Point = normalizedToPolar(Vector2.of(i+1, i).divide(grid));
				Vector2 grid4Point = normalizedToPolar(Vector2.of(i+1, i+1).divide(grid));

				Vector3 grid1Cartesian = Vector3.ofPolar(grid1Point.x, grid1Point.y, 1.0);
				Vector3 grid2Cartesian = Vector3.ofPolar(grid2Point.x, grid2Point.y, 1.0);
				Vector3 grid3Cartesian = Vector3.ofPolar(grid3Point.x, grid3Point.y, 1.0);
				Vector3 grid4Cartesian = Vector3.ofPolar(grid4Point.x, grid4Point.y, 1.0);
				
				double gridSize1 = grid1Cartesian.subtract(grid2Cartesian).getLength();
				double gridSize2 = grid1Cartesian.subtract(grid3Cartesian).getLength();
				double gridSize3 = grid3Cartesian.subtract(grid4Cartesian).getLength();

				gridSizes[i] = Math.min(gridSize1, Math.min(gridSize2, gridSize3));
			}
		}

		@Override
		public double calculateCraters(double latitude, double longitude, long[] seed, PlanetData planetData, PlanetGenerationContext context) {
			Vector2 normalizedPoint = polarToNormalized(Vector2.of(latitude, longitude));
			Vector2 big = normalizedPoint.multiply(grid);
			Vector2 bigFloor = big.floor();

			double gridSize = gridSizes[(int)bigFloor.x] * planetData.radius;
			if (gridSize == 0) {
				return 0;
			}

			seed[seed.length - 2] = (long)bigFloor.x;
			seed[seed.length - 1] = (long)bigFloor.y;
			Random random = new Random(seed);
			
			if (random.nextDouble() > planetData.craterDensity) {
				return 0;
			}
			
			double randomSize = random.nextDouble(0.1, 0.49);
			gridSize *= randomSize;

			Vector2 randomDisplacement = Vector2.of(
				random.nextDouble(randomSize/2, 1.0 - randomSize/2),
				random.nextDouble(randomSize/2, 1.0 - randomSize/2));
			Vector2 normalizedCraterPoint = bigFloor.add(randomDisplacement).divide(grid);
			Vector2 craterPoint = normalizedToPolar(normalizedCraterPoint);

			Vector3 pointCartesian = Vector3.ofPolar(latitude, longitude, planetData.radius);
			Vector3 craterCartesian = Vector3.ofPolar(craterPoint.x, craterPoint.y, planetData.radius);
			double distance = pointCartesian.subtract(craterCartesian).getLength();

			double relativeDistance = distance / gridSize;
			if (relativeDistance > 1.0) {
				return 0;
			}

			double radialNoiseLevel = crater.radialNoiseFunction.calculate(relativeDistance);
			if (radialNoiseLevel > 0) {
				double craterAngleSin = normalizedPoint.subtract(normalizedCraterPoint).y / relativeDistance;
				double craterAngle = Math.asin(craterAngleSin);
				double radialNoise = radialNoiseValue.polarValue(craterAngle, 1.0, 0.0001);
				radialNoise = MathUtil.smoothstep(0, 1, radialNoise);
				relativeDistance *= 1.0 + radialNoiseLevel * radialNoise;
				if (relativeDistance > 1.0) {
					return 0;
				}
			}
			
			double height = crater.heightFunction.calculate(relativeDistance);

			double heightNoiseLevel = crater.heightNoiseFunction.calculate(relativeDistance);
			if (heightNoiseLevel > 0) {
				double heightNoise = heightNoiseValue.sphereValue(latitude, longitude, context);
				height += heightNoise * heightNoiseLevel;
			}
			
			return height;
		}

		private Vector2 polarToNormalized(Vector2 polar) {
			return Vector2.of(
					polar.x / Planet.RANGE_LATITUDE,
					polar.y / Planet.RANGE_LONGITUDE);
		}

		private Vector2 normalizedToPolar(Vector2 normalized) {
			return Vector2.of(
					normalized.x * Planet.RANGE_LATITUDE,
					normalized.y * Planet.RANGE_LONGITUDE);
		}
	}
	
	public static class Crater {
		public final String name;
		public final CraterFunction heightFunction;
		public final CraterFunction heightNoiseFunction;
		public final CraterFunction radialNoiseFunction;

		public Crater(String name, CraterFunction heightFunction, CraterFunction heightNoiseFunction, CraterFunction radialNoiseFunction) {
			this.name = name;
			this.heightFunction = heightFunction;
			this.heightNoiseFunction = heightNoiseFunction;
			this.radialNoiseFunction = radialNoiseFunction;
		}
		
		@Override
		public String toString() {
			return name;
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
