package ch.obermuhlner.planetgen.planet.layer;

import java.util.List;
import java.util.function.Function;

import ch.obermuhlner.planetgen.math.MathUtil;
import ch.obermuhlner.planetgen.math.Vector2;
import ch.obermuhlner.planetgen.math.Vector3;
import ch.obermuhlner.planetgen.planet.DoubleMap;
import ch.obermuhlner.planetgen.planet.LayerType;
import ch.obermuhlner.planetgen.planet.Planet;
import ch.obermuhlner.planetgen.planet.PlanetGenerationContext;
import ch.obermuhlner.planetgen.value.NoisePolarValue;
import ch.obermuhlner.planetgen.value.NoiseVector2Value;
import ch.obermuhlner.util.Random;

public class CraterLayer implements Layer {

	private static final double NOT_YET_CALCULATED = Double.MIN_VALUE;
	
	private final List<CraterCalculator> craterCalculators;

	public CraterLayer(List<CraterCalculator> craterCalculators) {
		this.craterCalculators = craterCalculators;
	}

	@Override
	public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
		if (planet.planetData.craterDensity <= 0.0 && planet.planetData.volcanoDensity <= 0.0) {
			return;
		}
		
		int n = Math.min(craterCalculators.size(), context.craterLayerIndex);
		for (int i = 0; i < n; i++) {
			CraterCalculator craterCalculator = craterCalculators.get(i);
			craterCalculator.calculateCraters(planetPoint, planet, latitude, longitude, context, i);
		}
		
		planetPoint.height = planetPoint.groundHeight;
		planetPoint.isWater = planetPoint.height <= 0;
	}

	public static class CraterCalculator extends BasicCraterCalculator {
		private final double height;
		private final double grid;
		private final long seed;
		private final DensityFunction densityFunction;
		private final double[] gridSizesLatitude;
		private final double[] gridSizesLongitude;
		
		private final DoubleMap cachedCraterCenterHeight;
		
		public CraterCalculator(double height, int grid, long seed, DensityFunction densityFunction, Crater crater) {
			super(crater);
			
			this.height = height;
			this.grid = grid;
			this.densityFunction = densityFunction;
			this.seed = seed;
			
			gridSizesLatitude = new double[grid];
			gridSizesLongitude = new double[grid];
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

				gridSizesLatitude[i] = gridSize2;
				gridSizesLongitude[i] = Math.min(gridSize1, gridSize3);
			}

			if (grid < 1000) {
				cachedCraterCenterHeight = new DoubleMap(grid, grid, NOT_YET_CALCULATED);
			} else {
				cachedCraterCenterHeight = null;
			}
		}

		public void calculateCraters(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context, int craterLayerIndex) {
			//planetPoint.debug = densityFunction.density(latitude, longitude);
			
			if (height < context.accuracy) {
				return;
			}
			
			Vector2 normalizedPoint = polarToNormalized(Vector2.of(latitude, longitude));
			Vector2 big = normalizedPoint.multiply(grid);
			int gridX = (int) big.x;
			int gridY = (int) big.y;

			double gridSizeLatitude = gridSizesLatitude[gridY] * planet.planetData.radius;
			double gridSizeLongitude = gridSizesLongitude[gridX] * planet.planetData.radius;
			if (gridSizeLongitude == 0 || gridSizeLatitude == 0) {
				return;
			}

			long[] seeds = new long[3];
			seeds[0] = seed;
			seeds[1] = gridX;
			seeds[2] = gridY;
			Random random = new Random(seeds);
			
			double randomDensityValue = random.nextDouble();

			if (randomDensityValue > densityFunction.density()) {
				return;
			}

			double randomSize = random.nextDouble(0.1, 0.49);
			double craterSize = Math.min(gridSizeLatitude, gridSizeLongitude) * randomSize;

			double randomSizeX = craterSize / gridSizeLatitude;
			double randomSizeY = craterSize / gridSizeLongitude;
			Vector2 displacement = Vector2.of(
					random.nextDouble(randomSizeX, 1.0 - randomSizeX),
					random.nextDouble(randomSizeY, 1.0 - randomSizeY));
			Vector2 normalizedCraterPoint = Vector2.of(gridX, gridY).add(displacement).divide(grid);
			Vector2 craterCenterPoint = normalizedToPolar(normalizedCraterPoint);

			if (randomDensityValue > densityFunction.density(craterCenterPoint.x, craterCenterPoint.y)) {
				return;
			}

			Vector3 pointCartesian = Vector3.ofPolar(latitude, longitude, planet.planetData.radius);
			Vector3 craterCenterCartesian = Vector3.ofPolar(craterCenterPoint.x, craterCenterPoint.y, planet.planetData.radius);
			Vector3 craterPointCartesian = pointCartesian.subtract(craterCenterCartesian);

			double distance = craterPointCartesian.getLength();
			double relativeDistance = distance / craterSize;
			if (relativeDistance > 1.0) {
				return;
			}

			double craterAngle = craterPointCartesian.getLatitude();
			Vector2 surfaceCraterPoint = Vector2.ofPolar(craterAngle, relativeDistance);
			
			double heightFactor = randomSize * random.nextDouble(0.8, 1.2);
			double craterHeight = calculateCrater(surfaceCraterPoint, craterAngle, relativeDistance, context) * height * heightFactor;
			
			if (crater.backgroundMixEdge1 >= 0.0) {
				double craterCenterHeight = cachedCraterCenterHeight == null ? NOT_YET_CALCULATED : cachedCraterCenterHeight.getValue(gridX, gridY);
				if (craterCenterHeight == NOT_YET_CALCULATED) {
					PlanetGenerationContext heightContext = new PlanetGenerationContext();
					heightContext.layerTypes.add(LayerType.GROUND);
					heightContext.layerTypes.add(LayerType.CRATERS);
					heightContext.accuracy = context.accuracy;
					heightContext.craterLayerIndex = craterLayerIndex;
					
					craterCenterHeight = planet.getPlanetPoint(craterCenterPoint.x, craterCenterPoint.y, heightContext).groundHeight;
					if (cachedCraterCenterHeight != null) {
						cachedCraterCenterHeight.setValue(gridX, gridY, craterCenterHeight);
					}
				}
				double mix = MathUtil.smoothstep(crater.backgroundMixEdge0, crater.backgroundMixEdge1, relativeDistance);				
				planetPoint.groundHeight = MathUtil.mix(craterCenterHeight + craterHeight, planetPoint.groundHeight + craterHeight, mix);
			} else {
				planetPoint.groundHeight = planetPoint.groundHeight + craterHeight;
			}
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
		protected Crater crater;

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
		public final double backgroundMixEdge0;
		public final double backgroundMixEdge1;

		public Crater(String name, CraterFunction heightFunction, CraterFunction verticalHeightNoiseFunction, CraterFunction radialNoiseFunction, NoiseVector2Value verticalHeightNoiseValue, NoisePolarValue radialNoiseValue, boolean distanceDependentRadialNoise, double backgroundMixEdge0, double backgroundMixEdge1) {
			this.name = name;
			this.heightFunction = heightFunction;
			this.verticalHeightNoiseFunction = verticalHeightNoiseFunction;
			this.radialNoiseFunction = radialNoiseFunction;
			this.verticalHeightNoiseValue = verticalHeightNoiseValue;
			this.radialNoiseValue = radialNoiseValue;
			this.distanceDependentRadialNoise = distanceDependentRadialNoise;
			this.backgroundMixEdge0 = backgroundMixEdge0;
			this.backgroundMixEdge1 = backgroundMixEdge1;
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
	
	public interface DensityFunction {
		/**
		 * Returns the general density, without knowing the concrete location of the crater.
		 * This function is called early in the calculation of a crater layer.
		 * @return the general density
		 */
		double density();
		
		/**
		 * Returns the density at a potential location of a crater.
		 * This function is called late in the calculation of a crater layer.
		 * @param latitude
		 * @param longitude
		 * @return the density at the specified location
		 */
		double density(double latitude, double longitude);
	}
}
