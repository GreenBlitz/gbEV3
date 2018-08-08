package org.greenblitz.gbEV3.commandbased;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greenblitz.gbEV3.common.StationAccessor;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.remote.ev3.RemoteEV3;

public class Robot {
	private static RemoteEV3 mBrick;

	public static final Logger getRobotLogger() {
		return LogManager.getLogger("robot");
	}

	private StationAccessor m_station;
	private boolean m_calledDisabledInit = false;

	public static void exit(int code) {
		Sound.buzz();
		System.exit(code);
	}

	public boolean isDisabled() {
		return m_station.isDisabled();
	}

	public boolean isEnabled() {
		return m_station.isEnabled();
	}

	public boolean isAutonomous() {
		return m_station.isAutonomous();
	}

	public boolean isTeleop() {
		return m_station.isTeleoperated();
	}

	public void robotInit() {
		getRobotLogger().warn("Default robotInit() method... Overload me!");
	}

	public void disabledInit() {
		getRobotLogger().warn("Default disabledInit() method... Overload me!");
	}

	public void teleopInit() {
		getRobotLogger().warn("Default teleopInit() method... Overload me!");
	}

	public void autonomousInit() {
		getRobotLogger().warn("Default autonomousInit() method... Overload me!");
	}

	private boolean m_isRpFirstRun = true;

	public void robotPeriodic() {
		if (m_isRpFirstRun) {
			getRobotLogger().warn("Default robotPeriodic() method... Overload me!");
			m_isRpFirstRun = false;
		}
	}

	private boolean m_isDpFirstRun = true;

	public void disabledPeriodic() {
		if (m_isDpFirstRun) {
			getRobotLogger().warn("Default disabledPeriodic() method... Overload me!");
			m_isDpFirstRun = false;
		}
	}

	private boolean m_isTpFirstRun = true;

	public void teleopPeriodic() {
		if (m_isTpFirstRun) {
			getRobotLogger().warn("Default teleopPeriodic() method... Overload me!");
			m_isTpFirstRun = false;
		}
	}

	private boolean m_isApFirstRun = true;

	public void autonomousPeriodic() {
		if (m_isApFirstRun) {
			getRobotLogger().warn("Default autonomousPeriodic() method... Overload me!");
			m_isApFirstRun = false;
		}
	}

	private void competitionPeriodic() {
		if (isDisabled()) {
			if (!m_calledDisabledInit) {
				disabledInit();
				m_calledDisabledInit = true;
			} else {
				disabledPeriodic();
			}
		}

		robotPeriodic();

		if (isEnabled()) {
			switch (m_station.getCurrentGameType()) {
			case AUTO:
				if (m_isApFirstRun) {
					autonomousInit();
					m_isApFirstRun = false;
				}
				autonomousPeriodic();
				break;
			case TELEOP:
				if (m_isTpFirstRun) {
					teleopInit();
					m_isTpFirstRun = false;
				}
				teleopPeriodic();
				break;
			default:
				throw new IllegalStateException("Illegal game mode: " + m_station.getCurrentGameType());
			}

			m_calledDisabledInit = false;
		}
	}

	private static RemoteEV3 getBrick(String ip) {
		try {
			return new RemoteEV3(ip);
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			Robot.getRobotLogger().fatal("an error occured while trying to connect ot remote ev3 brick at ip " + ip, e);
		}
		return null;
	}

	public static RemoteEV3 getBrick() {
		return mBrick;
	}

	public static void gbEV3_MAIN(Robot robot) {
		getRobotLogger().info("Initializing robot!");
		StationAccessor.init();
		robot.m_station = StationAccessor.getInstance();
		/*
		 * String addr = robot.m_station.getRemoteIp(); while (addr == null) {
		 * addr = robot.m_station.getRemoteIp(); }
		 */
		String addr = "10.0.1.1";
		mBrick = getBrick(addr);
		Sound.beep();
		try {
			robot.robotInit();
		} catch (Throwable t) {
			getRobotLogger().fatal("Robots don't quit, but yours did!", t);
			Robot.exit(-1);
		}
		try {
			getRobotLogger().info("Robot is running!");

			while (Button.ESCAPE.isUp()) {
				robot.m_station.waitForData();
				robot.competitionPeriodic();
			}

			robot.m_station.close();
		} catch (Throwable t) {
			getRobotLogger().fatal("Robots don't quit, but yours did!", t);
			Robot.exit(-1);
		}
	}
}
