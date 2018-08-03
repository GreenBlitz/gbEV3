package org.greenblitz.gbEV3.tests;

import lejos.hardware.lcd.LCD;

import org.greenblitz.gbEV3.commandbased.Robot;

public class TestRobot extends Robot {

	public TestRobot(int period) {
		super(period);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void robotInit(){
		logger.fine("Init boyo");
	}
	
	@Override
	public void teleopPeriodic(){
		LCD.clearDisplay();
		LCD.drawInt((int) System.currentTimeMillis(), 0, 0);
	}

}
