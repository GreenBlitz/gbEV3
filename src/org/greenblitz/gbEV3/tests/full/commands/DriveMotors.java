package org.greenblitz.gbEV3.tests.full.commands;

import org.greenblitz.gbEV3.commandbased.Command;
import org.greenblitz.gbEV3.common.joystick.Axis;
import org.greenblitz.gbEV3.common.joystick.Joystick;
import org.greenblitz.gbEV3.tests.full.subsystem.Chassis;

public class DriveMotors extends Command {
	
	private Joystick stick;
	
	public DriveMotors() {
		requires(Chassis.getInstance());
	}
	
	@Override
	protected void initialize() {
		stick = new Joystick(0);
	};
	
	@Override
	protected void execute() {
		Chassis.getInstance().tankDrive((int)(stick.getAxis(Axis.LY) * 700), (int)(stick.getAxis(Axis.RY) * 700));
	}

	@Override
	protected boolean isFinished() {
		return false;
	}

}
