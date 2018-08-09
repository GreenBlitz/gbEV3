package org.greenblitz.gbEV3.common;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import lejos.hardware.DeviceException;
import lejos.remote.ev3.RMIRegulatedMotor;

import org.greenblitz.gbEV3.commandbased.Robot;

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

	public static void closeAllMotors() {
		for (RobotMotor motor : activeInstances.values()) {
			motor.stop();
			motor.close();
		}
	}

	public void stop() {
		try {
			mRegulatedMotor.setSpeed(0);
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(
					"an error occured while trying to stop motor", e);
		}
	}

	private void close() {
		try {
			mRegulatedMotor.close();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(
					"an error occured while trying to close motor", e);
		}
	}

	private RobotMotor(RobotMotorPort port) {
		try {
			synchronized (instancesLock) {
				mRegulatedMotor = Robot.getBrick().createRegulatedMotor(
						port.name, 'M');
				activeInstances.put(port, this);
				flt(true);
			}
		} catch (DeviceException e) {
			Robot.getRobotLogger().fatal(
					"unable to instantiate robot motor at port " + port
							+ ", try restarting the robot!", e);
			Robot.exit(-2, Thread.currentThread().getStackTrace()[0]);
		}
	}

	public void flt(boolean immediateReturn) {
		try {
			mRegulatedMotor.flt(immediateReturn);
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
		}
	}

	public void waitComplete() {
		try {
			mRegulatedMotor.waitComplete();
		} catch (RemoteException e) {

			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
			;
		}
	}

	public int getLimitAngle() {
		try {
			return mRegulatedMotor.getLimitAngle();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
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
			if (!mRegulatedMotor.isMoving()) {
				if (speed < 0)
					mRegulatedMotor.backward();
				else
					mRegulatedMotor.forward();
			} else {
				if (speed < 0 && mRegulatedMotor.getSpeed() > 0)
					mRegulatedMotor.backward();
				if (speed > 0 && mRegulatedMotor.getSpeed() < 0)
					mRegulatedMotor.forward();
			}
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
		}
	}

	public int getSpeed() {
		try {
			return mRegulatedMotor.getSpeed();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
			return 0;
		}
	}

	public float getMaxSpeed() {
		try {
			return mRegulatedMotor.getMaxSpeed();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
			return 0;
		}
	}

	public boolean isStalled() {
		try {
			return mRegulatedMotor.isStalled();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
			return false;
		}
	}

	public void setStallThreshold(int error, int time) {
		try {
			mRegulatedMotor.setStallThreshold(error, time);
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
		}
	}

	public void setAcceleration(int acceleration) {
		try {
			mRegulatedMotor.setAcceleration(acceleration);
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
		}
	}

	public void resetTachoCount() {
		try {
			mRegulatedMotor.resetTachoCount();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
		}
	}

	public int getTachoCount() {
		try {
			return mRegulatedMotor.getTachoCount();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
			return 0;
		}
	}

	public boolean isMoving() {
		try {
			return mRegulatedMotor.isMoving();
		} catch (RemoteException e) {
			Robot.getRobotLogger().fatal(e);
			Robot.exit(-3, Thread.currentThread().getStackTrace()[0]);
			return false;
		}
	}
}
