package org.greenblitz.gbEV3.common;

public enum RobotMotorPort {
	A("A"), B("B"), C("C"), D("D");
	
	public final String name;
	
	private RobotMotorPort(String portName) {
		name = portName;
	}
}
