package test;

import org.greenblitz.gbEV3.commandbased.Subsystem;

public class IntegerSubsystem extends Subsystem {
	private static final IntegerSubsystem instance = new IntegerSubsystem();
	
	public static IntegerSubsystem getInstance() { return instance; }
	
	private IntegerSubsystem() {}	
	
	@Override
	protected void initDefaultCommand() {
		setDefaultCommand(new IntegerCommand(100));
	}
	
	private int m_value = 0;
	
	public int getValue() { return m_value; }
	
	public void incrementValue() { m_value += 1; }	
}
