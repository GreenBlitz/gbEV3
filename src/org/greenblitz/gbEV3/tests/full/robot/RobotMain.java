package org.greenblitz.gbEV3.tests.full.robot;

import org.greenblitz.gbEV3.commandbased.Robot;
import org.greenblitz.gbEV3.commandbased.Scheduler;
import org.greenblitz.gbEV3.tests.full.subsystem.Chassis;

public class RobotMain extends Robot {

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
		Robot.getRobotLogger().fine("teleop init called");
	}

	@Override
	public void teleopPeriodic() {
		Scheduler.getInstance().run();
	}

	public static void main(String[] agrs) {
		gbEV3_MAIN(new RobotMain());
	}
}
