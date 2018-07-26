package org.greenblitz.gbEV3.commandbased;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/*
 * Part of the essential parts of the Command Base design pattern.
 */
public abstract class Command {
	
	private class Requirements implements Iterable<Subsystem> {
		@Override
		public Iterator<Subsystem> iterator() {
			return m_requirements.iterator();
		}
	}

	private final Set<Subsystem> m_requirements = new HashSet<>();

	private volatile boolean m_initialized = false;
	private volatile boolean m_running = false;
	private volatile boolean m_complete = false;
	private final boolean m_interruptible;
	private volatile boolean m_ended = false;

	public Command(boolean interruptible) {
		m_interruptible = interruptible;
	}

	public Command(boolean interruptible, Subsystem... requirements) {
		this(interruptible);
		for (Subsystem required : requirements) 
			if (!requires(required))
				throw new IllegalArgumentException("cannot require null subsystem");
	}
	
	public void start() {
		Scheduler.getInstance().add(this);
	}
	

	public final void run() {
		if (m_initialized)
			_initialize();

		if (shouldRun()) {
			m_running = true;

			execute();

			if (isFinished()) {
				_ended();
			}
		}
	}

	public final boolean terminate(boolean clean) {
		if (!m_interruptible && !clean)
			return false;

		m_running = false;
		m_ended = true;

		if (clean) {
			m_complete = true;
			ended();
		} else {
			interrupted();
		}

		return true;
	}

	public boolean isInitialized() {
		return m_initialized;
	}

	public boolean isRunning() {
		return m_running;
	}

	public boolean isCompleted() {
		return m_complete;
	}

	public boolean isInterruptible() {
		return m_interruptible;
	}

	public boolean isEnded() {
		return m_ended;
	}

	public final boolean shouldRun() {
		return !isCompleted() && !(isEnded() && isInterruptible());
	}

	public final boolean doesRequire(Subsystem subsystem) {
		return m_requirements.contains(subsystem);
	}

	public final Requirements getRequirements() {
		return new Requirements();
	}

	protected final boolean requires(Subsystem subsystem) {
		if (subsystem == null)
			return false;

		System.out.println("requires" + subsystem);
		m_requirements.add(subsystem);
		return true;
	}

	protected void initialize() {
	}

	private void _initialize() {
		initialize();
		m_initialized = true;
	}

	protected abstract void execute();

	protected abstract boolean isFinished();

	protected void ended() {
	}

	private void _ended() {
		m_complete = true;
		m_running = false;
		m_ended = true;
		ended();
	}

	protected void interrupted() {
		ended();
	}
}
