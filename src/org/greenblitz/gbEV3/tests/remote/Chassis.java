package org.greenblitz.gbEV3.tests.remote;

import org.greenblitz.gbEV3.commandbased.Subsystem;
import org.greenblitz.gbEV3.common.RobotMotor;
import org.greenblitz.gbEV3.common.RobotMotorPort;

public class Chassis extends Subsystem {

	private static Chassis boyy;

	public static Chassis getChassis() {
		if (boyy == null)
			boyy = new Chassis();
		return boyy;
	}

	RobotMotor left;
	RobotMotor right;

	private Chassis() {
		left = RobotMotor.at(RobotMotorPort.A);
		right = RobotMotor.at(RobotMotorPort.B);
	}

	public void tankDrive(float a, float b) {
		left.set(a);
		right.set(b);
	}

	@Override
	protected void initDefaultCommand() {
		setDefaultCommand(null);
	}

}
