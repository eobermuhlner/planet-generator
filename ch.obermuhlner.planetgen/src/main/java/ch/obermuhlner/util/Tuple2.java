/*
 * Copyright Profidata AG. All rights reserved.
 */

package ch.obermuhlner.util;

import java.io.Serializable;
import java.util.Objects;

/**
 * A tuple of two values.
 * 
 * <p>Tuples are convenient for various things, the most common is to use them as return values that contain multiple separate values.</p>
 * 
 * @param <T1> the type of the first value
 * @param <T2> the type of the second value
 * 
 * @see Tuples#tuple(Object, Object)
 */
public class Tuple2<T1, T2> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final T1 value1;
	private final T2 value2;

	/**
	 * Constructs a {@link Tuple2}.
	 * 
	 * @param theValue1 the first value
	 * @param theValue2 the second value
	 * @see Tuples#tuple(Object, Object)
	 */
	private Tuple2(T1 theValue1, T2 theValue2) {
		value1 = theValue1;
		value2 = theValue2;
	}

	/**
	 * Returns the first value.
	 * 
	 * @return the first value, may be <code>null</code>
	 */
	public T1 getValue1() {
		return value1;
	}

	/**
	 * Returns the second value.
	 * 
	 * @return the second value, may be <code>null</code>
	 */
	public T2 getValue2() {
		return value2;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value1, value2);
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof Tuple2)) {
			return false;
		}

		Tuple2<?, ?> other = (Tuple2<?, ?>) object;
		return (value1 == null ? other.value1 == null : value1.equals(other.value1)) && //
				(value2 == null ? other.value2 == null : value2.equals(other.value2));
	}

	@Override
	public String toString() {
		return "(" + value1 + "," + value2 + ")";
	}
	
	public static <T1, T2> Tuple2<T1, T2> of(T1 value1, T2 value2) {
		return new Tuple2<T1, T2>(value1, value2);
	}
}
