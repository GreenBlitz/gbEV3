package org.greenblitz.gbEV3.tests;

import lejos.hardware.lcd.LCD;

import org.greenblitz.gbEV3.commandbased.Robot;
import org.greenblitz.gbEV3.common.RobotClock;

public class TestRobot extends Robot {	
	@Override
	public void disabledPeriodic(){
		LCD.clearDisplay();
		LCD.drawInt((int) RobotClock.currentTimeMillis(), 0, 0);
	}

	public static void main(String[] args){
		gbEV3_MAIN(new TestRobot());
	}
}
