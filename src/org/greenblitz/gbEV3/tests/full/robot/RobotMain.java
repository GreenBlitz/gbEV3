package org.greenblitz.gbEV3.tests.full.robot;

import lejos.hardware.lcd.LCD;

import org.greenblitz.gbEV3.commandbased.Robot;
import org.greenblitz.gbEV3.commandbased.Scheduler;
import org.greenblitz.gbEV3.common.joystick.Axis;
import org.greenblitz.gbEV3.common.joystick.Joystick;
import org.greenblitz.gbEV3.tests.full.subsystem.Chassis;

public class RobotMain extends Robot {
	
	Joystick j;

	@Override
	public void robotInit() {
		Chassis.getInstance().initialize();
		j = new Joystick(0);
	}

	@Override
	public void robotPeriodic() {
	}

	@Override
	public void teleopInit() {
		System.out.println("teleop");
		Robot.getRobotLogger().fine("teleop init called");
	}

	@Override
	public void teleopPeriodic() {
		LCD.clear();
		Scheduler.getInstance().run();
		int i = (int)(j.getAxis(Axis.LY)*100);
		LCD.drawInt(i, 4, 4);
		Scheduler.getInstance().run();
	}

	public static void main(String[] agrs) {
		gbEV3_MAIN(new RobotMain());
	}
}
