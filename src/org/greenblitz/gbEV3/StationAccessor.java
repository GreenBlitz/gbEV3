package org.greenblitz.gbEV3;

public final class StationAccessor {
	
	public static StationAccessor getInstance() { return instance; }

	private static final StationAccessor instance = new StationAccessor(); 
	
	private StationAccessor() {}
	
	public boolean isDisabled() {
		return false;
	}
	
	public boolean isEnabled() {
		return !isDisabled();
	}
	
	public long currentGameTime() {
		return 0;
	}
	
	public void waitForData() {
	}
	
	public boolean isAutonomous() {
		return false;
	}
	
	public boolean isTeleop() {
		return false;
	}
}
