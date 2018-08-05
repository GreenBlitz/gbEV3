package org.greenblitz.gbEV3.common;

public class Tuple {
	public static <T, V> Tuple2<T, V> of(T first, V second) {
		return new Tuple2<T, V>(first, second);
	}
	
	public static <I, D, O> Tuple3<I, D, O> of(I first, D second, O third) {
		return new Tuple3<I, D, O>(first, second, third);
	}
}
