package org.greenblitz.gbEV3.tests.full.subsystem;

import org.greenblitz.gbEV3.commandbased.Robot;
import org.greenblitz.gbEV3.commandbased.Subsystem;
import org.greenblitz.gbEV3.tests.full.commands.DriveMotors;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

public class Chassis extends Subsystem {

	private EV3LargeRegulatedMotor left;
	private EV3LargeRegulatedMotor right;

	private static final Chassis chassis = new Chassis();

	public static final Chassis getInstance() {
		return chassis;
	}

	public Chassis() {
		left = new EV3LargeRegulatedMotor(MotorPort.A);
		right = new EV3LargeRegulatedMotor(MotorPort.B);
	}

	public void tankDrive(int speedLeft, int speedRight) {
		left.setSpeed(speedLeft);
		right.setSpeed(speedRight);
		Robot.getRobotLogger().fine("Right chassis: " + speedRight + " Left chassis: " + speedLeft);
	}

	@Override
	protected void initDefaultCommand() {
		setDefaultCommand(new DriveMotors());
	}

}
