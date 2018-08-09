package org.greenblitz.gbEV3.tests.remote;

import org.greenblitz.gbEV3.commandbased.Robot;
import org.greenblitz.gbEV3.commandbased.Scheduler;
import org.greenblitz.gbEV3.common.joystick.Button;
import org.greenblitz.gbEV3.common.joystick.Joystick;

public class RotateByJoystickTestRobot extends Robot {
	
	Joystick reee;
	
	@Override
	public void robotInit() {
		reee = Joystick.atPort(0);
		Robot.getRobotLogger().debug("Called Robot init");
		Chassis.getChassis().initialize();
		reee.whenPressed(Button.A, new TurnLeft(1000));
		reee.whenPressed(Button.B, new TurnRight(1000));
	}
	
	@Override
	public void teleopInit() {
		Robot.getRobotLogger().debug("Called Teleop init");
	}
	
	@Override
	public void teleopPeriodic() {
	}
	
	public static void main(String[] args){
		gbEV3_MAIN(new RotateByJoystickTestRobot());
	}
}
