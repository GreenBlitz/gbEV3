package org.greenblitz.gbEV3.commandbased;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import lejos.hardware.Button;
import lejos.hardware.Sound;

import org.greenblitz.gbEV3.common.StationAccessor;

public class Robot {
	protected static final Logger logger = Logger.getLogger("robot");

	public static Logger getRobotLogger() {
		return logger;
	}

	protected final StationAccessor m_station = StationAccessor.getInstance();
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
		return m_station.isTeleop();
	}

	public void robotInit() {
		logger.warning("Default robotInit() method... Overload me!");
	}

	public void disabledInit() {
		logger.warning("Default disabledInit() method... Overload me!`");
	}

	public void teleopInit() {
		logger.warning("Default teleopInit() method... Overload me!");
	}

	public void autonomousInit() {
		logger.warning("Default autonomousInit() method... Overload me!");
	}

	private boolean m_isRpFirstRun = true;

	public void robotPeriodic() {
		if (m_isRpFirstRun) {
			logger.warning("Default robotPeriodic() method... Overload me!");
			m_isRpFirstRun = false;
		}
	}

	private boolean m_isDpFirstRun = true;

	public void disabledPeriodic() {
		if (m_isDpFirstRun) {
			logger.warning("Default disabledPeriodic() method... Overload me!");
			m_isDpFirstRun = false;
		}
	}

	private boolean m_isTpFirstRun = true;

	public void teleopPeriodic() {
		if (m_isTpFirstRun) {
			logger.warning("Default teleopPeriodic() method... Overload me!");
			m_isTpFirstRun = false;
		}
	}

	private boolean m_isApFirstRun = true;

	public void autonomousPeriodic() {
		if (m_isApFirstRun) {
			logger.warning("Default autonomousPeriodic() method... Overload me!");
			m_isApFirstRun = false;
		}
	}

	public void loopFunc() {
		logger.finest("Its alive! Im running " + m_station.getGameType());

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
			switch (m_station.getGameType()) {
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
				throw new IllegalStateException("Illegal game mode: "
						+ m_station.getGameType());
			}

			m_calledDisabledInit = false;
		}
	}

	public static void gbEV3_MAIN(Robot robot) {
		try {
			logger.setUseParentHandlers(false);
			FileHandler fHndl = new FileHandler("robot.log", false);
			fHndl.setFormatter(new SimpleFormatter());
			fHndl.setLevel(Level.FINE);
			logger.addHandler(fHndl);
			logger.setLevel(Level.ALL);
			StationAccessor.init();
		} catch (SecurityException | IOException e) {
			logger.severe(e.toString());
			Robot.exit(-1);
		}

		Sound.beep();
		try {
			robot.robotInit();

			logger.info("Robot running!");
			Sound.beep();

			while (Button.ESCAPE.isUp()) {
				robot.m_station.waitForData(255);
				robot.loopFunc();
			}
		} catch (Throwable t) {
			logger.log(Level.SEVERE, "Error: ", t);
			Sound.buzz();
		}
	}
}
