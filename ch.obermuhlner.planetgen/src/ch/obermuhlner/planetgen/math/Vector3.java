package ch.obermuhlner.planetgen.math;

import java.util.Objects;

public class Vector3 {

	public final double x;
	public final double y;
	public final double z;

	private Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double length() {
		return Math.sqrt(lengthSquared());
	}

	public double lengthSquared() {
		return x*x + y*y + z*z;
	}
	
	public Vector3 add(Vector3 vector) {
		return of(
			this.x + vector.x,
			this.y + vector.y,
			this.z + vector.z);
	}
	
	public Vector3 add(double value) {
		return of(
			this.x + value,
			this.y + value,
			this.z + value);
	}
	
	public Vector3 multiply(double value) {
		return of(
			this.x * value,
			this.y * value,
			this.z * value);
	}
	
	public Vector3 divide(double value) {
		return of(
			this.x / value,
			this.y / value,
			this.z / value);
	}

	public double dot(Vector3 vector) {
		return this.x*vector.x + this.y*vector.y + this.z*vector.z;
	}
	
	public Vector3 cross(Vector3 vector) {
		return of(
			this.y*vector.z - this.z*vector.y,
			this.z*vector.x - this.x*vector.z,
			this.x*vector.y - this.y*vector.x);
	}
	
	public Vector3 normalize() {
		double len = length();
		return of(
				x / len,
				y / len,
				z / len);
	}
	
	public Vector3 clamp(double min, double max) {
		return of(
				MathUtil.clamp(x, min, max),
				MathUtil.clamp(y, min, max),
				MathUtil.clamp(z, min, max));
	}
	
	/**
	 * Interpolates between this vector and the specified end vector.
	 * 
	 * @param end the end vector
	 * @param weight the weight to interpolate between the two vectors between 0.0 and 1.0
	 * @return the interpolated vector
	 */
	public Vector3 interpolate(Vector3 end, double weight) {
		if (weight <= 0.0) {
			return this;
		}
		if (weight >= 1.0) {
			return end;
		}
		return of(
				x + (end.x - x) * weight,
				y + (end.y - y) * weight,
				z + (end.z - z) * weight);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector3 other = (Vector3) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + "," + z + ")";
	}

	public static Vector3 of(double x, double y, double z) {
		return new Vector3(x, y, z);
	}
}
