package org.greenblitz.gbEV3.tests.remote;

import java.awt.MouseInfo;
import java.awt.Point;

import org.greenblitz.gbEV3.commandbased.Command;
import org.greenblitz.gbEV3.commandbased.Robot;

public class DriveByMouse extends Command {
	// 960 540
	public final static Point center = new Point(682, 383);
	
	public static float vel;
	public static float vela;
	public static float velb;
	
	public DriveByMouse() {
		requires(Chassis.getChassis());
	}
	
	@Override
	public void initialize(){
		vela = 0;
		velb = 0;
	}
	
	@Override
	protected void execute() {
		Point p = MouseInfo.getPointerInfo().getLocation();
		
		// total speed of robot
		vel = (center.y - p.y) / (float)center.y;
		
		//direction
		vela = (p.x / 682f) - 1f;
		velb = 1f - vela;
		
		Robot.getRobotLogger().debug("mouse point: " + p + " ,vel: " + vel + " , vela: " + vela + " , velb " + velb);
		Chassis.getChassis().tankDrive(vela, velb);
	}

	@Override
	protected boolean isFinished() {
		// infinite loop
		return false;
	}

}
