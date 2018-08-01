package ch.obermuhlner.planetgen.math;

import java.util.Objects;

public class Vector2 {

	public final double x;
	public final double y;

	private Vector2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2 add(Vector2 vector) {
		return of(
			this.x + vector.x,
			this.y + vector.y);
	}
	
	public Vector2 add(double value) {
		return of(
			this.x + value,
			this.y + value);
	}
	
	public Vector2 subtract(Vector2 vector) {
		return of(
			this.x - vector.x,
			this.y - vector.y);
	}
	
	public Vector2 subtract(double value) {
		return of(
			this.x - value,
			this.y - value);
	}
	
	public Vector2 multiply(double value) {
		return of(
			this.x * value,
			this.y * value);
	}
	
	public Vector2 divide(double value) {
		return of(
			this.x / value,
			this.y / value);
	}

	public double dot(Vector2 vector) {
		return this.x*vector.x + this.y*vector.y;
	}

	public double getLength() {
		return Math.sqrt(getLengthSquared());
	}

	public double getLengthSquared() {
		return x*x + y*y;
	}
	
	public double getAngle() {
		return Math.atan2(y, x);
	}

	public Vector2 normalize() {
		double len = getLength();
		return of(
				x / len,
				y / len);
	}
	
	public Vector2 clamp(double min, double max) {
		return of(
				MathUtil.clamp(x, min, max),
				MathUtil.clamp(y, min, max));
	}
	
	public Vector2 floor() {
		return of(
				Math.floor(x),
				Math.floor(y));
	}
	
	/**
	 * Interpolates between this vector and the specified end vector.
	 * 
	 * @param end the end vector
	 * @param weight the weight to interpolate between the two vectors between 0.0 and 1.0
	 * @return the interpolated vector
	 */
	public Vector2 interpolate(Vector2 end, double weight) {
		if (weight <= 0.0) {
			return this;
		}
		if (weight >= 1.0) {
			return end;
		}
		return of(
				x + (end.x - x) * weight,
				y + (end.y - y) * weight);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		Vector2 other = (Vector2) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	public static Vector2 of(double x, double y) {
		return new Vector2(x, y);
	}
	
	
	public static Vector2 ofPolar(double angle, double radius) {
    	return of(
    		radius * Math.cos(angle),
    		radius * Math.sin(angle));
	}
}
