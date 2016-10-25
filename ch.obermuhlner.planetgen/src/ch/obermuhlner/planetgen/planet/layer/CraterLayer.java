package ch.obermuhlner.planetgen.planet.layer;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetData;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.NoisePolarValue;
import ch.obermuhlner.planetgen.value.NoiseVector2Value;
import ch.obermuhlner.util.Random;

public class CraterLayer implements Layer {

	private final List<CraterCalculator> craterCalculators;

	public CraterLayer(List<CraterCalculator> craterCalculators) {
		this.craterCalculators = craterCalculators;
	}

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		if (planet.planetData.craterDensity <= 0.0 && planet.planetData.volcanoDensity <= 0.0) {
			return;
		}
		
		long[] seed = new long[planet.planetData.seed.length + 2]; // add 2 empty entries in seed - will be filled later with crater coords
		System.arraycopy(planet.planetData.seed, 0, seed, 0, planet.planetData.seed.length);
		
		for (CraterCalculator craterCalculator : craterCalculators) {
			planetPoint.groundHeight += craterCalculator.calculateCraters(planetPoint, planet, latitude, longitude, seed, planet.planetData, context);
		}
		
		planetPoint.height = planetPoint.groundHeight;
	}

	public static interface CraterCalculator {
		double calculateCraters(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, long[] seed, PlanetData planetData, PlanetGenerationContext context);
	}

	public static class GridCartesianCraterCalculator extends BasicCraterCalculator implements CraterCalculator {
		private final double height;
		private final double grid;
		private final DoubleSupplier densityFunction;
		private double[] gridSizes;
		
		public GridCartesianCraterCalculator(double height, int grid, DoubleSupplier densityFunction, Crater crater) {
			super(crater);
			
			this.height = height;
			this.grid = grid;
			this.densityFunction = densityFunction;
			
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
		public double calculateCraters(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, long[] seed, PlanetData planetData, PlanetGenerationContext context) {
			if (height < context.accuracy) {
				return 0;
			}
			
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
			
			if (random.nextDouble() > densityFunction.getAsDouble()) {
				return 0;
			}
			
			double randomSize = random.nextDouble(0.1, 0.49);
			gridSize *= randomSize;

			Vector2 randomDisplacement = Vector2.of(
				random.nextDouble(randomSize/2, 1.0 - randomSize/2),
				random.nextDouble(randomSize/2, 1.0 - randomSize/2));
			Vector2 normalizedCraterPoint = bigFloor.add(randomDisplacement).divide(grid);
			Vector2 craterCenterPoint = normalizedToPolar(normalizedCraterPoint);

			Vector3 pointCartesian = Vector3.ofPolar(latitude, longitude, planetData.radius);
			Vector3 craterCenterCartesian = Vector3.ofPolar(craterCenterPoint.x, craterCenterPoint.y, planetData.radius);
			Vector3 craterPointCartesian = pointCartesian.subtract(craterCenterCartesian);

			double distance = craterPointCartesian.getLength();
			double relativeDistance = distance / gridSize;
			if (relativeDistance > 1.0) {
				return 0;
			}

			double craterAngle = craterPointCartesian.getLatitude();
			Vector2 surfaceCraterPoint = Vector2.ofPolar(craterAngle, relativeDistance);
			
			return calculateCrater(surfaceCraterPoint, craterAngle, relativeDistance, context) * height;
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
	
	public static class BasicCraterCalculator {
		private Crater crater;

		public BasicCraterCalculator(Crater crater) {
			this.crater = crater;
		}

		public double calculateCrater(Vector2 craterPoint, PlanetGenerationContext context) {
			double distance = craterPoint.getLength();
			if (distance > 1.0) {
				return 0;
			}
			
			double angle = craterPoint.getAngle();
			
			return calculateCrater(craterPoint, angle, distance, context);
		}
		
		public double calculateCrater(Vector2 craterPoint, double angle, double distance, PlanetGenerationContext context) {
			if (distance > 1.0) {
				return 0;
			}

			double radialNoiseLevel = crater.radialNoiseFunction.calculate(distance);
			if (radialNoiseLevel > 0) {
				double noiseDistance = crater.distanceDependentRadialNoise ? distance : 1.0;
				double radialNoise = crater.radialNoiseValue.polarValue(angle, noiseDistance, 0.0001);
				radialNoise = MathUtil.smoothstep(0, 1, radialNoise);
				distance *= 1.0 + radialNoiseLevel * radialNoise;
				if (distance > 1.0) {
					return 0;
				}
			}
			
			double height = crater.heightFunction.calculate(distance);

			double heightNoiseLevel = crater.verticalHeightNoiseFunction.calculate(distance);
			if (heightNoiseLevel > 0) {
				double heightNoise = crater.verticalHeightNoiseValue.vector2Value(craterPoint, context);
				height += heightNoise * heightNoiseLevel;
			}
			
			return height;
		}		
	}
	
	public static class Crater {
		public final String name;
		public final CraterFunction heightFunction;
		public final CraterFunction verticalHeightNoiseFunction;
		public final CraterFunction radialNoiseFunction;
		public final NoiseVector2Value verticalHeightNoiseValue;
		public final NoisePolarValue radialNoiseValue;
		public final boolean distanceDependentRadialNoise;

		public Crater(String name, CraterFunction heightFunction, CraterFunction verticalHeightNoiseFunction, CraterFunction radialNoiseFunction, NoiseVector2Value verticalHeightNoiseValue, NoisePolarValue radialNoiseValue, boolean distanceDependentRadialNoise) {
			this.name = name;
			this.heightFunction = heightFunction;
			this.verticalHeightNoiseFunction = verticalHeightNoiseFunction;
			this.radialNoiseFunction = radialNoiseFunction;
			this.verticalHeightNoiseValue = verticalHeightNoiseValue;
			this.radialNoiseValue = radialNoiseValue;
			this.distanceDependentRadialNoise = distanceDependentRadialNoise;
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
}
