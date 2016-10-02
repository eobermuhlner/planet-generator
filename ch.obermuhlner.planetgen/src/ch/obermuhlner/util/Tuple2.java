/*
 * Copyright Profidata AG. All rights reserved.
 */

package ch.obermuhlner.util;

import java.io.Serializable;

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

	private static final long serialVersionUID = 1087567343343376526L;

	private final T1 value1;
	private final T2 value2;

	private transient int hashCode;

	/**
	 * Constructs a {@link Tuple2}.
	 * 
	 * @param theValue1 the first value
	 * @param theValue2 the second value
	 * @see Tuples#tuple(Object, Object)
	 */
	public Tuple2(T1 theValue1, T2 theValue2) {
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
		if (hashCode == 0) {
			int hash = 7;
			hash = 31 * hash + (value1 == null ? 23 : value1.hashCode());
			hash = 31 * hash + (value2 == null ? 23 : value2.hashCode());
			hashCode = hash;
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object theObject) {
		if (theObject == this) {
			return true;
		}
		if (!(theObject instanceof Tuple2)) {
			return false;
		}

		Tuple2<?, ?> theOther = (Tuple2<?, ?>) theObject;
		return (value1 == null ? theOther.value1 == null : value1.equals(theOther.value1)) && //
				(value2 == null ? theOther.value2 == null : value2.equals(theOther.value2));
	}

	@Override
	public String toString() {
		return "(" + value1 + "," + value2 + ")";
	}
}
