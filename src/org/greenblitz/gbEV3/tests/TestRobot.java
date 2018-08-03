package org.greenblitz.gbEV3.tests;

import lejos.hardware.lcd.LCD;

import org.greenblitz.gbEV3.commandbased.Robot;

public class TestRobot extends Robot {	
	@Override
	public void disabledPeriodic(){
		LCD.clearDisplay();
		LCD.drawInt((int) System.currentTimeMillis(), 0, 0);
	}

	public static void main(String[] args){
		run(new TestRobot());
	}
}
