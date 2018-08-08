package org.greenblitz.gbEV3.common;

public interface MessageAnalyzer<T> {
	boolean test(String msg);
	T parse(String msg);
}
