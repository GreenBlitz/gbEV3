package org.greenblitz.gbEV3.commandbased;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.greenblitz.gbEV3.common.StationAccessor;

public final class Scheduler {

	private static class FillerCommand extends Command {

		static final FillerCommand INACTIVE = new FillerCommand();

		private FillerCommand() {
		}

		@Override
		protected void execute() {
		}

		@Override
		protected boolean isFinished() {
			return true;
		}

		public String toString() {
			return "Filler Command";
		}
	}

	private static final Scheduler instance = new Scheduler();

	public static Scheduler getInstance() {
		return instance;
	}

	private final Map<Subsystem, Command> mCurrentCommands = new ConcurrentHashMap<>();
	private final List<Command> mAddedCommands = new LinkedList<Command>();

	private final Object mAddedCommandsMutex = new Object();

	private final StationAccessor m_station = StationAccessor.getInstance();

	private boolean removeCommand(Command command, boolean clean) {
		if (!command.terminate(clean)) {
			return false;
		}

		for (Subsystem requirement : command.getRequirements()) {
			mCurrentCommands.put(requirement, requirement.getDefaultCommand());
		}
		
		Robot.getRobotLogger().fine("removing command " + command);
		return true;
	}

	public void run() {
		if (m_station.isDisabled())
			return;

		for (Subsystem system : mCurrentCommands.keySet())
			system.update();

		for (Subsystem system : mCurrentCommands.keySet())
			if (mCurrentCommands.get(system) == FillerCommand.INACTIVE) {
				mCurrentCommands.put(system, system.getDefaultCommand());
				Robot.getRobotLogger().fine("added command " + system.getDefaultCommand());
			}

		for (Command cmd : new HashSet<Command>(mCurrentCommands.values())) {
			Robot.getRobotLogger().finest("should run command '" + cmd + "', will run it? " + cmd.shouldRun());
			if (cmd.shouldRun()) {
				cmd.run();
			} else {
				removeCommand(cmd, true);
			}
		}

		synchronized (mAddedCommandsMutex) {
			for (Command cmd : mAddedCommands)
				updateCurrentCommands(cmd);
			
			mAddedCommands.clear();
		}
	}

	public boolean add(Command command) {
		if (!checkRequirementsRegistered(command))
			return false;

		synchronized (mAddedCommandsMutex) {
			mAddedCommands.add(command);
		}
		return true;
	}

	private boolean updateCurrentCommands(Command command) {
		if (mCurrentCommands.containsValue(command))
			return false;

		if (!checkRequirements(command))
			return false;

		for (Command cmd : mCurrentCommands.values())
			for (Subsystem requirement : command.getRequirements())
				if (cmd.doesRequire(requirement))
					removeCommand(cmd, false); 
					

		for (Subsystem requirement : command.getRequirements())
			mCurrentCommands.put(requirement, command);
		
		Robot.getRobotLogger().fine("added command " + command);
		return true;
	}

	private boolean checkRequirements(Command command) {
		for (Command cmd : mCurrentCommands.values())
			for (Subsystem requirement : command.getRequirements())
				if (cmd.doesRequire(requirement) && !cmd.isInterruptible())
					return false;

		return true;
	}

	private boolean checkRequirementsRegistered(Command command) {
		for (Subsystem requirement : command.getRequirements())
			if (!mCurrentCommands.containsKey(requirement))
				return false;

		return true;
	}

	public void registerSubsystem(Subsystem system) {
		Robot.getRobotLogger().fine("registering subsystem " + system);
		mCurrentCommands.put(system, FillerCommand.INACTIVE);
	}

	public Command getCurrentCommand(Subsystem system) {
		Command ret = mCurrentCommands.get(system);
		return (ret == FillerCommand.INACTIVE ? null : ret);
	}
}
