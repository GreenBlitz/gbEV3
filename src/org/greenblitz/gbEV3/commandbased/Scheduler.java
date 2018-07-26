package org.greenblitz.gbEV3.commandbased;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.greenblitz.gbEV3.StationAccessor;

public final class Scheduler {
	private static final Scheduler instance = new Scheduler();
	
	public static Scheduler getInstance() { return instance; }
	
	private final Map<Subsystem, Command> m_currentCommands = new ConcurrentHashMap<>();
	private final List<Command> m_addedCommands = Collections.synchronizedList(new LinkedList<Command>());
	
	private final StationAccessor m_station = StationAccessor.getInstance();
	
	private boolean removeCommand(Command command, boolean clean) {
		 command.terminate(clean);
		
		for (Subsystem requirement : command.getRequirements()) {
			m_currentCommands.put(requirement, requirement.getDefaultCommand());
		}
		
		return true;
	}
	
	public void run() {
		if (m_station.isDisabled()) return;
		
		for (Subsystem system : m_currentCommands.keySet())
			system.update();
		
		for (Command cmd : new HashSet<Command>(m_currentCommands.values())) {
			if (cmd.shouldRun())
				cmd.run();
			else
				removeCommand(cmd, true);
		}
		
		for (Command cmd : m_addedCommands)
			updateCurrentCommands(cmd);
	}
	
	public boolean add(Command command) {
		if (!checkRequirementsRegistered(command))
			return false;
		
		m_addedCommands.add(command);
		return true;
	}
	
	private boolean updateCurrentCommands(Command command) {
		if (m_currentCommands.containsValue(command)) 
			return false;
		
		if (!checkRequirements(command)) 
			return false;
		
		for (Subsystem requirement : command.getRequirements()) 
			removeCommand(m_currentCommands.get(requirement), false);
		
		
		return true;
	}
	
	private boolean checkRequirements(Command command) {
		for (Subsystem requirement : command.getRequirements()) { 
			if (!m_currentCommands.containsKey(requirement))
				return false;
			
			if (!m_currentCommands.get(requirement).isInterruptible())
				return false;
		}
			
		return true;
	}
	
	private boolean checkRequirementsRegistered(Command command) {
		for (Subsystem requirement : command.getRequirements()) 
			if (!m_currentCommands.containsKey(requirement))
				return false;
		
		return true;
	}
	
	public void registerSubsystem(Subsystem system) {
		m_currentCommands.put(system, system.getDefaultCommand());
	}	
}
