package org.greenblitz.gbEV3.tests.commands;

import org.greenblitz.gbEV3.commandbased.Command;
import org.greenblitz.gbEV3.commandbased.Robot;
import org.greenblitz.gbEV3.common.joystick.Axis;
import org.greenblitz.gbEV3.common.joystick.Joystick;
import org.greenblitz.gbEV3.tests.subsystem.Chassis;

public class DriveMotors extends Command {

	private Joystick stick;

	public DriveMotors() {
		requires(Chassis.getInstance());
	}

	@Override
	protected void initialize() {
		stick = new Joystick(0);
	}

	@Override
	protected void execute() {
		/*
		 * Chassis.getInstance().tankDrive((int) (stick.getAxis(Axis.LY) * 700),
		 * (int) (stick.getAxis(Axis.RY) * 700)); Robot.getRobotLogger().debug(
		 * "driving [Right= " + ((int) (stick.getAxis(Axis.RY) * 700) +
		 * ", Left= " + ((int) (stick.getAxis(Axis.LY) * 700))) + "]");
		 */

		Chassis.getInstance().tankDrive(0.8f, 0.8f);
		Robot.getRobotLogger().debug("driving [Right= " + 0.8 + ", Left= " + 0.8 + "]");
	}

	@Override
	protected boolean isFinished() {
		return false;
	}
}
