package org.greenblitz.gbEV3.tests.reactiontim;

import org.greenblitz.gbEV3.commandbased.Robot;

public class ReactionTimeTestRobot extends Robot {
	
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
		gbEV3_MAIN(new ReactionTimeTestRobot());
	}
}
