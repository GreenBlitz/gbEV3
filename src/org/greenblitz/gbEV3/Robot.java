package org.greenblitz.gbEV3;

public abstract class Robot {

	protected final StationAccessor m_station = StationAccessor.getInstance();
	
	protected static enum ActivationMode {
		AUTONOMOUS,
		TELEOP,
		DISABLED,
		NONE
	}
	
	protected final double m_period;

	private ActivationMode m_lastMode = ActivationMode.NONE;
	
	public Robot(double period) {
		m_period = period;
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
	    System.out.println("Default robotInit() method... Overload me!");
	}
	
	public void disabledInit() {
		System.out.println("Default disabledInit() method... Overload me!`");
	}
	
	public void teleopInit() {
	    System.out.println("Default teleopInit() method... Overload me!");
	}
	
	public void autonomousInit() {
	    System.out.println("Default autonomousInit() method... Overload me!");
	}
	
	
	private boolean m_isRpFirstRun = true;
	public void robotPeriodic() {
		if (m_isRpFirstRun) {
		      System.out.println("Default robotPeriodic() method... Overload me!");
		      m_isRpFirstRun = false;
		}
	}
	
	private boolean m_isDpFirstRun = true;
	public void disabledPeriodic() {
		if (m_isDpFirstRun) {
		      System.out.println("Default disabledPeriodic() method... Overload me!");
		      m_isDpFirstRun = false;
		}
	}
	
	private boolean m_isTpFirstRun = true;
	public void teleopPeriodic() {
		if (m_isTpFirstRun) {
		      System.out.println("Default teleopPeriodic() method... Overload me!");
		      m_isTpFirstRun = false;
		}
	}
	
	private boolean m_isApFirstRun = true;
	public void autonomousPeriodic() {
		if (m_isApFirstRun) {
		      System.out.println("Default autonomousPeriodic() method... Overload me!");
		      m_isApFirstRun = false;
		}
	}
}
