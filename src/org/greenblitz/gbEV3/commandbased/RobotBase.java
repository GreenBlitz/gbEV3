package org.greenblitz.gbEV3.commandbased;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.SocketHandler;
import java.util.logging.XMLFormatter;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Delay;

import org.greenblitz.gbEV3.common.StationAccessor;

public class RobotBase {

	protected final StationAccessor m_station = StationAccessor.getInstance();
	
	private static final RobotBase INSTANCE = new RobotBase(200);
	
	protected static final Logger logger = Logger.getLogger("Robot");
	
	static {
		try {
			FileHandler fHndl = new FileHandler("Robot.log", false);
			fHndl.setFormatter(new XMLFormatter());
			fHndl.setLevel(Level.ALL);
			logger.addHandler(fHndl);
			
			SocketHandler sHndl = new SocketHandler("10.0.2.2",4590);
			sHndl.setFormatter(new SimpleFormatter());
			sHndl.setLevel(Level.INFO);
			logger.addHandler(sHndl);			
			logger.setLevel(Level.ALL);
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			logger.severe(e.getMessage() + " " + e.toString());
			Sound.buzz();
		}
		
	}
	
	protected final int m_miliPeriod;
	
	public RobotBase(int period) {
		m_miliPeriod = period;
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
	
	public void startCompetition() {
		logger.finest("Its alive! Im running" + m_station.getGameType());
		
		//auto
		switch(m_station.getGameType()){
		case AUTO:
			if(m_isApFirstRun) autonomousInit();
			autonomousPeriodic();
			break;
		case TELEOP:
			if(m_isTpFirstRun) teleopInit();
			teleopPeriodic();
			break;
		default:
			throw new IllegalStateException("Illegal game mode: " + m_station.getGameType());
		}
		
	}
	
	public static void main(String[] args){
		
		//set new logger
		System.setOut(new PrintStream(new OutputStream() {
			StringBuilder mem = new StringBuilder();
			@Override
			public void write(int b) throws IOException {
				byte bytes = (byte) (b & 0xff);
		        mem.append(bytes);

		        if (mem.toString().endsWith(System.lineSeparator())) {
		            mem.deleteCharAt(mem.length () - System.lineSeparator().length());
		            flush();
		        }
			}
			
			public void flush () {
		        logger.log (Level.INFO, mem.toString());
		        mem.delete(0, mem.length() - 1);
		    }
		}));
		
		try{
			logger.info("Robot running!");
			logger.info("Hello John, Im ev3");
			Sound.beep();
			
			INSTANCE.robotInit();
			
			StationAccessor.getInstance().waitForData();
			
			while(Button.ESCAPE.isUp()) {
				INSTANCE.robotPeriodic();
				boolean firstDisabled = false;
				
				if(INSTANCE.isEnabled()){
					INSTANCE.startCompetition();
					firstDisabled = false;
				}
				
				else 
					if(!firstDisabled){
					INSTANCE.disabledInit();
					firstDisabled = true;
					}
					else {
						INSTANCE.disabledPeriodic();
					}
				
				Delay.msDelay(INSTANCE.m_miliPeriod);
			}
		} catch(Throwable t){
			logger.severe(t.getMessage() +" "+ t.toString());
			Sound.buzz();
		}
		
	}
}
