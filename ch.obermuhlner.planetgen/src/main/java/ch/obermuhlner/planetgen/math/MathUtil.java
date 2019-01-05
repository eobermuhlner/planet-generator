package ch.obermuhlner.planetgen.math;

import ch.obermuhlner.util.Tuple2;

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
	
	public static double wrap(double value, double range) {
		if (value < 0) {
			return value + range; 
		}
		return value % range;
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
	
	public static double smoothfloor(double edge0, double edge1, double x) {
		double floor = Math.floor(x);
		double fract = x - floor;
		return floor + smoothstep(edge0, edge1, fract);
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

	public static double higher(double value) {
		return higher(value, 1.0);
	}
	
	public static double higher(double value, double maxValue) {
		return higher(value, maxValue, 0.5);
	}
	
	public static double higher(double value, double maxValue, double factor) {
		return value + (maxValue - value) * factor;
	}

	public static double nextPowerOfTen(double value) {
		double result = 1.0;
		while (result < value) {
			result *= 10.0;
		}
		return result;
	}

	public static double deviationDistance(double value, double optimum, double minusDeviation, double plusDeviation) {
		double distance = value - optimum;
		distance /= distance > 0 ? plusDeviation : minusDeviation;
		distance = Math.abs(distance);
		return distance;
	}

	private static final float ZERO_THRESHOLD = 1e-4f;
	
	public static float maybeZero(float value) {
		return maybeZero(value, ZERO_THRESHOLD);
	}
	
	public static float maybeZero(float value, float threshold) {
		return value < ZERO_THRESHOLD && value > -ZERO_THRESHOLD ? 0 : value;
	}

	public static double exaggerate(double value, double factor) {
		return exaggerate(value, 1.0, factor);
	}

	public static double exaggerate(double value, double center, double factor) {
		return (value - center) * factor + center;
	}

	public static double closesPointInLine(double startX, double startY, double endX, double endY, double pointX, double pointY) {
		double lineDx = endX - startX;
		double lineDy = endY - startY;
		double pointDx = pointX - startX;
		double pointDy = pointY - startY;
		double lineLengthSquared = lineDx*lineDx + lineDy*lineDy;

		if (lineLengthSquared < 0.0000001) {
			return 0;
		}

		double t = (pointDx*lineDx + pointDy*lineDy) / lineLengthSquared;
		t = clamp(t, 0, 1);
		return t;
	}

	public static class ClosestPoint {
		public final double value;
		public final Vector2 closest;
		public final double distanceSquared;

		public ClosestPoint(double value, Vector2 closest, double distanceSquared) {
			this.value = value;
			this.closest = closest;
			this.distanceSquared = distanceSquared;
		}
	}
	public static ClosestPoint closestPoint(Vector2 start, Vector2 end, Vector2 point) {
		Vector2 line = end.subtract(start);
		Vector2 lineToPoint = point.subtract(start);
		double lineLengthSquared = line.getLengthSquared();

		if (lineLengthSquared < 0.0000001) {
			return new ClosestPoint(0.0, start, lineToPoint.getLengthSquared());
		}

		double t = line.dot(lineToPoint) / lineLengthSquared;
		Vector2 closest;

		if (t < 0) {
			t = 0;
			closest = start;
		} else if (t >= 1.0) {
			t = 1.0;
			closest = end;
		} else {
			closest = start.add(line.multiply(t));
		}

		return new ClosestPoint(t, closest, point.subtract(closest).getLengthSquared());
	}

	public static class Intersection {
		public final double t1;
		public final double t2;
		public final Vector2 point;

		public Intersection(double t1, double t2, Vector2 point) {
			this.t1 = t1;
			this.t2 = t2;
			this.point = point;
		}
	}

	public static Intersection lineIntersection(Vector2 start1, Vector2 end1, Vector2 start2, Vector2 end2) {
		return lineIntersection(start1, end1, start2, end2, 0.0);
	}

	public static Intersection lineIntersection(Vector2 start1, Vector2 end1, Vector2 start2, Vector2 end2, double epsilon) {
		double denom = (start1.x - end1.x) * (start2.y - end2.y) - (start1.y - end1.y) * (start2.x - end2.x);
		if (Math.abs(denom) < 0.0000000001) {
			return null;
		}

		double nomT1 = (start1.x - start2.x) * (start2.y - end2.y) - (start1.y - start2.y) * (start2.x - end2.x);
		double t1 = nomT1 / denom;

		if (t1 < 0-epsilon || t1 > 1+epsilon) {
			return null;
		}

		double nomT2 = (start1.x - end1.x) * (start1.y - start2.y) - (start1.y - end1.y) * (start1.x - start2.x);
		double t2 = nomT2 / denom;

		if (t2 < 0-epsilon || t2 > 1+epsilon) {
			return null;
		}

		Vector2 point = Vector2.of(start1.x + t1*(end1.x - start1.x), start1.y + t1*(end1.y - start1.y));
		return new Intersection(t1, t2, point);
	}
}
