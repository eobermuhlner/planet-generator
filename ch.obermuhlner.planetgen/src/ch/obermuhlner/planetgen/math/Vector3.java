package ch.obermuhlner.planetgen.math;

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
		return add(this, vector);
	}
	
	public Vector3 add(double value) {
		return add(this, value);
	}
	
	public Vector3 multiply(double value) {
		return multiply(this, value);
	}
	
	public Vector3 divide(double value) {
		return divide(this, value);
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
	
	@Override
	public String toString() {
		return "(" + x + "," + y + "," + z + ")";
	}

	public static Vector3 of(double x, double y, double z) {
		return new Vector3(x, y, z);
	}
	
	public static Vector3 cross(Vector3 vector1, Vector3 vector2) {
		return of(
				vector1.y*vector2.z - vector1.z*vector2.y,
				vector1.z*vector2.x - vector1.x*vector2.z,
				vector1.x*vector2.y - vector1.y*vector2.x);
	}

	public static double dot(Vector3 vector1, Vector3 vector2) {
		return vector1.x*vector2.x + vector1.y*vector2.y + vector1.z*vector2.z;
	}

	public static Vector3 add(Vector3 vector, double value) {
		return of(
				vector.x + value,
				vector.y + value,
				vector.z + value);
	}

	public static Vector3 multiply(Vector3 vector, double value) {
		return of(
				vector.x * value,
				vector.y * value,
				vector.z * value);
	}

	public static Vector3 divide(Vector3 vector, double value) {
		return of(
				vector.x / value,
				vector.y / value,
				vector.z / value);
	}

	public static Vector3 add(Vector3 vector1, Vector3 vector2) {
		return of(
				vector1.x + vector2.x,
				vector1.y + vector2.y,
				vector1.z + vector2.z);
	}
}
