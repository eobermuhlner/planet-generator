package ch.obermuhlner.planetgen.math;

public class MathUtil {

	public static double sec(double x) {
		return 1.0 / Math.cos(x);
	}
	
	public static double cosec(double x) {
		return 1.0 / Math.sin(x);
	}
	
	public static float mix(float x, float y, float weight) {
		return x * (1-weight) + y * weight;
	}
	
	public static double mix(double x, double y, double weight) {
		return x * (1-weight) + y * weight;
	}
	
	public static float clamp(float value, float min, float max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	public static double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	/**
	 * The smoothstep function returns 0.0 if x is smaller then edge0 and 1.0 if x is larger than edge1.
	 * Otherwise the return value is interpolated between 0.0 and 1.0 using Hermite polynomirals.
	 * 
	 * @param edge0 the lower edge
	 * @param edge1 the upper edge
	 * @param x the value to smooth
	 * @return the smoothed value
	 */
	public static float smoothstep (float edge0, float edge1, float x) {
		float clamped = clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
		return clamped * clamped * (3 - 2 * clamped);
	}

	public static double smoothstep (double edge0, double edge1, double x) {
		double clamped = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
		return clamped * clamped * (3 - 2 * clamped);
	}
	
	public static float transform(float fromMin, float fromMax, float toMin, float toMax, float value) {
		if (value < fromMin) {
			return toMin;
		}
		if (value > fromMax) {
			return toMax;
		}
		return (value - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin;
	}

	public static double transform(double fromMin, double fromMax, double toMin, double toMax, double value) {
		if (value < fromMin) {
			return toMin;
		}
		if (value > fromMax) {
			return toMax;
		}
		return (value - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin;
	}

	public static double nextPowerOfTen(double value) {
		double result = 1.0;
		while (result < value) {
			result *= 10.0;
		}
		return result;
	}

	private static final float ZERO_THRESHOLD = 1e-4f;
	
	public static float maybeZero(float value) {
		return maybeZero(value, ZERO_THRESHOLD);
	}
	
	public static float maybeZero(float value, float threshold) {
		return value < ZERO_THRESHOLD && value > -ZERO_THRESHOLD ? 0 : value;
	}
}
