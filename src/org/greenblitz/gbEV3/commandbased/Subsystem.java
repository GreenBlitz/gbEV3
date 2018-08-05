package org.greenblitz.gbEV3.commandbased;

public abstract class Subsystem {
	private volatile Command m_defaultCommand;
	private volatile boolean m_initialized = false;
	
	protected Subsystem() {
		Scheduler.getInstance().registerSubsystem(this);
	}
	
	public void update() {}
	
	public void initialize() {}
	
	public Command getDefaultCommand() {
		if (!m_initialized) {
			initDefaultCommand();
			m_initialized = true;
		}
			
		return m_defaultCommand;
	}
	
	protected abstract void initDefaultCommand();
	
	public boolean setDefaultCommand(Command command) {
		if (command == null)
			m_defaultCommand = new NoneCommand(this);
		
		if (!command.doesRequire(this) || command.getRequirementCount() != 1) 
				return false;
		
		m_defaultCommand = command;
		return true;
	}
	
	public Command getCurrentCommand() {
		return Scheduler.getInstance().getCurrentCommand(this);
	}
}
