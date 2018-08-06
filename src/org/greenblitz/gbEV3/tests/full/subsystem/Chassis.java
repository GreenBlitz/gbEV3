package org.greenblitz.gbEV3.tests.full.subsystem;

import org.greenblitz.gbEV3.commandbased.Robot;
import org.greenblitz.gbEV3.commandbased.Subsystem;
import org.greenblitz.gbEV3.tests.full.commands.DriveMotors;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;

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
		if (!left.isMoving() && speedLeft != 0) {
			startEngine(left, speedLeft > 0);
		}
		if (!right.isMoving() && speedRight != 0) {
			startEngine(right, speedRight > 0);
		}
	}
	
	private void startEngine(RegulatedMotor m, boolean direction) {
		if (direction)
			m.forward();
		else
			m.backward();
			
	}

	@Override
	protected void initDefaultCommand() {
		setDefaultCommand(new DriveMotors());
	}

}
