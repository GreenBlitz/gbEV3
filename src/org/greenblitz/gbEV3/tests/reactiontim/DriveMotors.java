package org.greenblitz.gbEV3.tests.reactiontim;

import org.greenblitz.gbEV3.commandbased.Command;

public class DriveMotors extends Command {

	long t1;
	long t2;
	
	public DriveMotors(long timeout1, long timeout2) {
		requires(Chassis.getInstance());
		this.t1 = timeout1;
		this.t2 = timeout2;
	}

	@Override
	protected void initialize() {
		t1 += System.currentTimeMillis();
		t2 += t1;
	}

	@Override
	protected void execute() {
		if (System.currentTimeMillis() < t1) 
			Chassis.getInstance().tankDrive(1, 1);
		else if (System.currentTimeMillis() < t2)
			Chassis.getInstance().tankDrive(-1, -1);
	}

	@Override
	protected boolean isFinished() {
		return System.currentTimeMillis() > t2;
	}
	
	@Override
	protected void ended() {
		Chassis.getInstance().tankDrive(0, 0);
	}
}
