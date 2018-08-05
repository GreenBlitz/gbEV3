package org.greenblitz.gbEV3.common;

public class Tuple3<I, D, O> {
	public I first;
	public D second;
	public O third;
	
	public Tuple3(I first, D second, O third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}
}
