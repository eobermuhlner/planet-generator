package ch.obermuhlner.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Random {

	private final java.util.Random[] randoms; 
	
	private int index;

	public Random(long seed) {
		this(new long[] { seed });
	}

	
	public Random(long[] seeds) {
		randoms = new java.util.Random[seeds.length];
		
		long seed = 0; 
		for (int i = 0; i < seeds.length; i++) {
			seed = seed * 31 + i + seeds[i];
			java.util.Random random = new java.util.Random(seed);
			randoms[i] = random;
		}
		
		int warmups = new java.util.Random(seed).nextInt(seeds.length) + seeds.length;
		for (int i = 0; i < warmups; i++) {
			nextInt();
		}
	}
	
	public int nextInt() {
		return nextRandom().nextInt();
	}
	
	public int nextInt(int range) {
		if (range == 0) {
			return 0;
		}
		return nextRandom().nextInt(range);
	}
	
	public int nextInt(int min, int max) {
		return nextInt(max - min) + min;
	}
	
	public long nextLong() {
		return nextRandom().nextLong();
	}

	public boolean nextBoolean() {
		return nextRandom().nextBoolean();
	}
	
	public boolean nextBoolean(double probabilityTrue) {
		return nextRandom().nextDouble() < probabilityTrue;
	}
	
	public float nextFloat() {
		return nextRandom().nextFloat();
	}
	
	public float nextFloat(float range) {
		return nextFloat() * range;
	}
	
	public float nextFloat(float min, float max) {
		return nextFloat() * (max - min) + min;
	}
	
	public double nextDouble() {
		return nextRandom().nextDouble();
	}
	
	public double nextDouble(double range) {
		return nextDouble() * range;
	}
	
	public double nextDouble(double min, double max) {
		return nextDouble() * (max - min) + min;
	}
	
	public double nextGaussian() {
		return nextRandom().nextGaussian();
	}

	public double nextGaussian(double meanValue) {
		return nextGaussian(meanValue, meanValue / 10);
	}
	
	public double nextGaussian(double meanValue, double standardDeviation) {
		return nextGaussian() * standardDeviation + meanValue;
	}

	@SafeVarargs
	public final <T> Map<T, Double> nextProbabilityMap(Probability<T>... probabilities) {
		double factor[] = new double[probabilities.length];
		double sum = 0;
		for (int i = 0; i < factor.length; i++) {
			factor[i] = probabilities[i].probability;
			sum += factor[i];
		}
		
		Map<T, Double> result = new HashMap<T, Double>();
		if (sum != 0) {
			for (int i = 0; i < factor.length; i++) {
				double percent = factor[i]/sum;
				if (percent > 0) {
					result.put(probabilities[i].value, percent);
				}
			}
		}
		return result;
	}
	
	@SafeVarargs
	public final <T> T nextProbability(Probability<T>... probabilities) {
		return nextProbability(Arrays.asList(probabilities));
	}

	public final <T> T nextProbability(List<Probability<T>> probabilities) {
		double total = 0;
		for (Probability<T> p : probabilities) {
			total += p.probability;
		}
		double r = nextDouble(total);

		double partial = 0;
		for (Probability<T> p : probabilities) {
			if (r > partial && r <= partial+p.probability) {
				return p.value;
			}
			partial += p.probability;
		}
		
		return null;
	}

	@SafeVarargs
	public final <T> T next(T... values) {
		return values[nextInt(values.length)];
	}
	
	private java.util.Random nextRandom() {
		index++;
		if (index >= randoms.length) {
			index = 0;
		}
		return randoms[index];
	}
	
	public static class Probability<T> {
		public final double probability;
		public final T value;
		
		public Probability(double probability, T value) {
			this.probability = probability;
			this.value = value;
		}
		
		@Override
		public String toString () {
			return "(" + probability + "," + value + ")";
		}
	}
	
	public static <T> Probability<T> p(double probability, T value) {
		return new Probability<T>(probability, value);
	}
}
