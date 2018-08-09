package org.greenblitz.gbEV3.tests.drive.subsystem;

import org.greenblitz.gbEV3.commandbased.Subsystem;
import org.greenblitz.gbEV3.common.RobotMotor;
import org.greenblitz.gbEV3.common.RobotMotorPort;
import org.greenblitz.gbEV3.tests.drive.commands.DriveMotors;

import lejos.hardware.port.MotorPort;

public class Chassis extends Subsystem {

	private RobotMotor left;
	private RobotMotor right;

	private static Chassis chassis;
	private static final Object instanceMutex = new Object();

	public static final Chassis getInstance() {
		synchronized (instanceMutex) {
			if (chassis == null)
				chassis = new Chassis();
			
			return chassis;
		}
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
