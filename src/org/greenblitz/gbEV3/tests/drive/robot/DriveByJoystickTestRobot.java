package org.greenblitz.gbEV3.tests.drive.robot;

import org.greenblitz.gbEV3.commandbased.Robot;
import org.greenblitz.gbEV3.commandbased.Scheduler;
import org.greenblitz.gbEV3.tests.drive.subsystem.Chassis;

public class DriveByJoystickTestRobot extends Robot {
	
	@Override
	public void robotInit() {
		Chassis.getInstance().initialize();
	}

	@Override
	public void robotPeriodic() {
	}

	@Override
	public void teleopInit() {
		System.out.println("teleop");
		Robot.getRobotLogger().debug("teleop init called");
	}

	@Override
	public void teleopPeriodic() {
	}

	public static void main(String[] agrs) {
		gbEV3_MAIN(new DriveByJoystickTestRobot());
	}
}
