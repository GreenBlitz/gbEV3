package org.greenblitz.gbEV3.tests.remote;

import org.greenblitz.gbEV3.commandbased.Command;

public class TurnRight extends Command {


	long t0;
	long timeout;
	
	public TurnRight(long timeout) {
		requires(Chassis.getChassis());
	
		this.timeout = timeout;
	}
	
	@Override
	protected void initialize() {
		t0 = System.currentTimeMillis();
	}
	
	@Override
	protected void execute() {
		Chassis.getChassis().tankDrive(1, -1);
	}

	@Override
	protected boolean isFinished() {
		return System.currentTimeMillis() - t0 > timeout;
	}

	@Override
	protected void ended() {
		Chassis.getChassis().tankDrive(0, 0);
	}	

}
