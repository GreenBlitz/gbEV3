package org.greenblitz.gbEV3.tests.drive.commands;

import org.greenblitz.gbEV3.commandbased.Command;
import org.greenblitz.gbEV3.commandbased.Robot;
import org.greenblitz.gbEV3.common.joystick.Axis;
import org.greenblitz.gbEV3.common.joystick.Joystick;
import org.greenblitz.gbEV3.tests.drive.subsystem.Chassis;

public class DriveMotors extends Command {

	private Joystick stick;

	public DriveMotors() {
		requires(Chassis.getInstance());
	}

	@Override
	protected void initialize() {
		stick = Joystick.atPort(0);
	}

	@Override
	protected void execute() {

		float left = stick.getAxis(Axis.LY);
		float right = stick.getAxis(Axis.RY);

		Chassis.getInstance().tankDrive(left, right);
		Robot.getRobotLogger().trace(
				"driving [Left= " + left + ", Right= " + right + "]");

	}

	@Override
	protected boolean isFinished() {
		return false;
	}
}
