package org.greenblitz.gbEV3.common;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.greenblitz.gbEV3.commandbased.Robot;

import lejos.remote.ev3.RMIRegulatedMotor;

public class RobotMotor {

	private static final Map<RobotMotorPort, RobotMotor> activeInstances = new HashMap<>();
	private static final Object instancesLock = new Object();

	private RMIRegulatedMotor mRegulatedMotor;

	public static RobotMotor at(RobotMotorPort port) {
		synchronized (instancesLock) {
			if (!activeInstances.containsKey(port))
				activeInstances.put(port, new RobotMotor(port));

			return activeInstances.get(port);
		}
	}
	
	private RobotMotor(RobotMotorPort port) {
		synchronized (instancesLock) {
			mRegulatedMotor = Robot.getBrick().createRegulatedMotor(port.name, 'M');
			activeInstances.put(port, this);
		}
	}

	public void flt(boolean immediateReturn) {
		try {
			mRegulatedMotor.flt(immediateReturn);
		} catch (RemoteException e) {

			Robot.getRobotLogger().fatal(e);
		}
	}

	public void waitComplete() {
		try {
			mRegulatedMotor.waitComplete();
		} catch (RemoteException e) {

			Robot.getRobotLogger().fatal(e);
			;
		}
	}

	public int getLimitAngle() {
		try {
			return mRegulatedMotor.getLimitAngle();
		} catch (RemoteException e) {

			Robot.getRobotLogger().fatal(e);
			return 0;
		}
	}

	/**
	 * Sets the power to the motor, as a part of the maximum power, when 1 is
	 * the maximum power forward and -1 is the maximum power backwards. values
	 * greater than 1 or smaller than -1 will be treated as 1 or -1.
	 * 
	 * @param power
	 */
	public void set(float power) {
		try {
			if (power == 0) {
				mRegulatedMotor.stop(true);
				return;
			}

			if (Math.abs(power) > 1)
				power /= Math.abs(power);
			
			int speed = (int) (getMaxSpeed() * power);
			mRegulatedMotor.setSpeed(speed);
			if (speed < 0)
				mRegulatedMotor.backward();
			else
				mRegulatedMotor.forward();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
		}
	}

	public int getSpeed() {
		try {
			return mRegulatedMotor.getSpeed();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			return 0;
		}
	}

	public float getMaxSpeed() {
		try {
			return mRegulatedMotor.getMaxSpeed();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			return 0;
		}
	}

	public boolean isStalled() {
		try {
			return mRegulatedMotor.isStalled();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			return false;
		}
	}

	public void setStallThreshold(int error, int time) {
		try {
			mRegulatedMotor.setStallThreshold(error, time);
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
		}
	}

	public void setAcceleration(int acceleration) {
		try {
			mRegulatedMotor.setAcceleration(acceleration);
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
		}
	}

	public void resetTachoCount() {
		try {
			mRegulatedMotor.resetTachoCount();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
		}
	}

	public int getTachoCount() {
		try {
			return mRegulatedMotor.getTachoCount();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			return 0;
		}
	}

	public boolean isMoving() {
		try {
			return mRegulatedMotor.isMoving();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			return false;
		}
	}
}
