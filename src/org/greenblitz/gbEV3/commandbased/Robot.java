package org.greenblitz.gbEV3.commandbased;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.greenblitz.gbEV3.common.StationAccessor;

import lejos.hardware.Button;
import lejos.hardware.Sound;

public class Robot {
	protected static Logger ROBOT_LOGGER;

	public static final Logger getRobotLogger() {
		return ROBOT_LOGGER;
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
		return m_station.isTeleop();
	}

	public void robotInit() {
		ROBOT_LOGGER.warning("Default robotInit() method... Overload me!");
	}

	public void disabledInit() {
		ROBOT_LOGGER.warning("Default disabledInit() method... Overload me!");
	}

	public void teleopInit() {
		ROBOT_LOGGER.warning("Default teleopInit() method... Overload me!");
	}

	public void autonomousInit() {
		ROBOT_LOGGER.warning("Default autonomousInit() method... Overload me!");
	}

	private boolean m_isRpFirstRun = true;

	public void robotPeriodic() {
		if (m_isRpFirstRun) {
			ROBOT_LOGGER.warning("Default robotPeriodic() method... Overload me!");
			m_isRpFirstRun = false;
		}
	}

	private boolean m_isDpFirstRun = true;

	public void disabledPeriodic() {
		if (m_isDpFirstRun) {
			ROBOT_LOGGER.warning("Default disabledPeriodic() method... Overload me!");
			m_isDpFirstRun = false;
		}
	}

	private boolean m_isTpFirstRun = true;

	public void teleopPeriodic() {
		if (m_isTpFirstRun) {
			ROBOT_LOGGER.warning("Default teleopPeriodic() method... Overload me!");
			m_isTpFirstRun = false;
		}
	}

	private boolean m_isApFirstRun = true;

	public void autonomousPeriodic() {
		if (m_isApFirstRun) {
			ROBOT_LOGGER.warning("Default autonomousPeriodic() method... Overload me!");
			m_isApFirstRun = false;
		}
	}

	public void loopFunc() {
		ROBOT_LOGGER.finest("Its alive! Im running " + m_station.getGameType());

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
			Robot.ROBOT_LOGGER = Logger.getLogger("robot");
			Robot.ROBOT_LOGGER.setUseParentHandlers(false);
			FileHandler fHndl = new FileHandler("robot.log", false);
			fHndl.setFormatter(new SimpleFormatter());
			fHndl.setLevel(Level.FINE);
			ROBOT_LOGGER.addHandler(fHndl);
			ROBOT_LOGGER.setLevel(Level.ALL);
		} catch (SecurityException | IOException e) {
			ROBOT_LOGGER.severe(e.toString());
			Robot.exit(-1);
		}
		ROBOT_LOGGER.config("Initializing robot!");
		StationAccessor.init();
		robot.m_station = StationAccessor.getInstance();
		Sound.beep();
		try {
			robot.robotInit();
		} catch (Throwable t) {
			ROBOT_LOGGER.severe("Robots don't quit, but yours did!");
			ROBOT_LOGGER.severe(t.toString());
			ROBOT_LOGGER.severe(Arrays.toString(t.getStackTrace()));
			Robot.exit(-1);
		}
		try {
			ROBOT_LOGGER.config("Robot is running!");

			while (Button.ESCAPE.isUp()) {
				robot.m_station.waitForData(255);
				robot.loopFunc();
			}
			
			robot.m_station.release();
		} catch (Throwable t) {
			ROBOT_LOGGER.severe("Robots don't quit, but yours did!");
			ROBOT_LOGGER.severe(t.toString());
			ROBOT_LOGGER.severe(Arrays.toString(t.getStackTrace()));
			Robot.exit(-1);
		}
	}
}
