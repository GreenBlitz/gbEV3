package test;

import org.greenblitz.gbEV3.commandbased.Command;

public class IntegerCommand extends Command {

	private int limit;
	
	public IntegerCommand(int limit, boolean interruptible) {
		super(interruptible, IntegerSubsystem.getInstance());
		this.limit = limit;
	}
	
	public IntegerCommand(int limit) {
		this(limit, true);
	}

	@Override
	protected void execute() {
		IntegerSubsystem.getInstance().incrementValue();
	}

	@Override
	protected boolean isFinished() {
		return IntegerSubsystem.getInstance().getValue() >= limit;
	}

	
}
