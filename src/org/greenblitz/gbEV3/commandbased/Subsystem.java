package org.greenblitz.gbEV3.commandbased;

public abstract class Subsystem {
	private volatile Command m_defaultCommand;
	private volatile boolean m_initialized = false;
	
	public void update() {}
	
	public Command getDefaultCommand() {
		if (!m_initialized) {
			initDefaultCommand();
			m_initialized = true;
		}
			
		return m_defaultCommand;
	}
	
	protected abstract void initDefaultCommand();
	
	public boolean setDefaultCommand(Command command) {
		if (!command.doesRequire(this)) 
				return false;
		
		m_defaultCommand = command;
		return true;
	}
	
}
