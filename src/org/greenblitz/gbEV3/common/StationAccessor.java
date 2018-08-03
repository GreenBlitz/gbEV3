package org.greenblitz.gbEV3.common;


public final class StationAccessor {
	
	public static StationAccessor getInstance() { return instance; }

	private static final StationAccessor instance = new StationAccessor(); 
	
	private GameType m_gameType;
	
	private boolean m_isEnabled;
	
	private StationAccessor() {
		m_gameType = null;
	}
	
	public boolean isDisabled() {
		return !m_isEnabled;
	}
	
	public boolean isEnabled() {
		return m_isEnabled;
	}
	
	public long currentGameTime() {
		return 0;
	}
	
	public void waitForData() {
		//TODO: actual implement
		m_isEnabled = true;
		m_gameType = GameType.AUTO;
	}
	
	public boolean isAutonomous() {
		return m_gameType == GameType.AUTO;
	}
	
	public boolean isTeleop() {
		return m_gameType == GameType.TELEOP;
	}
	
	public GameType getGameType(){
		return m_gameType;
	}
	
	
}
