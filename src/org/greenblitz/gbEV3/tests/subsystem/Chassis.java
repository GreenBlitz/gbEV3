package org.greenblitz.gbEV3.tests.subsystem;

import org.greenblitz.gbEV3.commandbased.Subsystem;
import org.greenblitz.gbEV3.common.RobotMotor;
import org.greenblitz.gbEV3.common.RobotMotorPort;
import org.greenblitz.gbEV3.tests.commands.DriveMotors;

import lejos.hardware.port.MotorPort;

public class Chassis extends Subsystem {

	private RobotMotor left;
	private RobotMotor right;

	private static final Chassis chassis = new Chassis();

	public static final Chassis getInstance() {
		return chassis;
	}

	public Chassis() {
		left = RobotMotor.at(RobotMotorPort.A);
		right = RobotMotor.at(RobotMotorPort.B);
	}

	public void tankDrive(float speedLeft, float speedRight) {
		left.set(speedLeft);
		right.set(speedRight);
	}

	@Override
	protected void initDefaultCommand() {
		setDefaultCommand(new DriveMotors());
	}

}
