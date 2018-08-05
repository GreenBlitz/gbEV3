package org.greenblitz.gbEV3.commandbased;

/**
 * Empty command - it never ends and does nothing
 * @author karlo
 *
 */
public class NoneCommand extends Command {
	
	public NoneCommand(boolean interruptible, Subsystem... system) {
		super(interruptible, system);
	}
	
	public NoneCommand(Subsystem... system) {
		this(true, system);
	}

	@Override
	protected void execute() {}

	@Override
	protected boolean isFinished() {
		return false;
	}	
}
