package org.greenblitz.gbEV3.common;

public enum RobotSensorPort {
	S1("S1"), S2("S2"), S3("S3"), S4("S4");

	public final String name;

	private RobotSensorPort(String portName) {
		name = portName;
	}
}
